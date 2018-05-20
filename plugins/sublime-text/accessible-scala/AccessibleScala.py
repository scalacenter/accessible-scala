import sublime
import sublime_plugin

import threading
import subprocess

import os
import re

client = None

def plugin_loaded():
  global client
  if not client:
    client = AccessibleScalaClient()

def plugin_unloaded():
  global client
  if client:
    del client

def runCommand(view, cmd):
  file = view.file_name()
  is_scala = (
    (file and file.endswith(".scala"))
     # or 
    # "Scala" in view.settings().get('syntax')
  )
  if is_scala and client:
    selections = view.sel()
    if selections:
      first = selections[0]
      start = str(first.begin())
      client.sendCmd(cmd, start, file)
      client.setView(view)

class AscalaSummaryCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "summary-at")

class AscalaDescribeCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "describe")

class AscalaBreadcrumbsCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "breadcrumbs")

class AscalaLeftCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "left")

class AscalaRightCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "right")

class AscalaUpCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "up")

class AscalaDownCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "down")

class AccessibleScalaClient():
  def __init__(self):
    here = os.path.dirname(os.path.realpath(__file__))
    options = "-Djava.library.path=" + os.environ['ESPEAK_LIB_PATH'] + ":" + here + "/bin"
    cmd = ["java", options, "-jar", "ascala.jar"]

    process = subprocess.Popen(cmd,
                               cwd=here,
                               universal_newlines=True,
                               bufsize=1,
                               stdin=subprocess.PIPE,
                               stderr=subprocess.PIPE,
                               stdout=subprocess.PIPE)
    self.process = process
    self.transport = StdioTransport(process)
    self.transport.start(self.receive_payload)
    self.view = None

  def __del__(self):
    self.process.terminate()

  def receive_payload(self, message):
    select_pattern = re.compile("select (\d*) (\d*) (\w*)")
    match = select_pattern.match(message)
    if match:
      start = int(match.group(1))
      end = int(match.group(2))
      className = match.group(3)
      # print(className)
      self.view.run_command('accessible_scala_set_selection', {'start': start, 'end': end})

  def sendCmd(self, verb, start, file):
    cmd = verb + " " + start + " " + file + "\n"
    self.transport.send(cmd)

  def sendCmd2(self, verb, file):
    cmd = verb + " " + file + "\n"
    self.transport.send(cmd)

  def sendCmd3(self, verb):
    cmd = verb + "\n"
    self.transport.send(cmd)

  def summary(self, file):
    self.sendCmd2("summary", file)

  def setView(self, view):
    self.view = view

class AccessibleScalaSetSelection(sublime_plugin.TextCommand):
  def run(self, edit, start, end):
    # print("clear")
    # print("add " + str(start) + " " + str(end))
    self.view.sel().clear()
    region = sublime.Region(start, end)
    self.view.sel().add(region)
    # self.view.show_at_center(start)
    (h, v) = self.view.text_to_layout(start)
    margin = 50
    self.view.set_viewport_position((h, v - margin))


class StdioTransport():
  def __init__(self, process):
      self.process = process

  def start(self, on_receive):
    self.on_receive = on_receive
    self.stdout_thread = threading.Thread(target=self.read_stdout)
    self.stdout_thread.start()

    self.stderr_thread = threading.Thread(target=self.read_stderr)
    self.stderr_thread.start()

  def read_stdout(self):
    self.read(self.process.stdout)

  def read_stderr(self):
    self.read(self.process.stderr)

  def read(self, stream):
    running = True
    while running:
      running = self.process.poll() is None
      try:
        content = stream.readline()
        if content:
          self.on_receive(content)

      except IOError as err:
        print("IOError", err)
        break

    print("plugin exited\n")

  def send(self, message):
    if self.process:
      try:
        self.process.stdin.write(message)
        self.process.stdin.flush()
      except (BrokenPipeError, OSError) as err:
        print("Failure writing to stdout", err)


