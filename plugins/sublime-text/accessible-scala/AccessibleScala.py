import sublime
import sublime_plugin

import threading
import subprocess

import os

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
  if file and file.endswith(".scala") and client:
    selections = view.sel()
    if selections:
      first = selections[0]
      start = str(first.begin())
      client.sendCmd(cmd, start, file)

class AscalaSummaryCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "summary-at")

class AscalaDescribeCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "describe")

class AscalaBreadcrumbsCommand(sublime_plugin.TextCommand):
  def run(self, edit):
    runCommand(self.view, "breadcrumbs")

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
    self.transport.start()

  def __del__(self):
    self.process.terminate()

  def sendCmd(self, verb, start, file):
    cmd = verb + " " + start + " " + file + "\n"
    self.transport.send(cmd)

  def sendCmd2(self, verb, file):
    cmd = verb + " " + file + "\n"
    self.transport.send(cmd)    

  def summary(self, file):
    self.sendCmd2("summary", file)

class StdioTransport():
  def __init__(self, process):
      self.process = process

  def start(self):
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
          print(content)

      except IOError as err:
        print("IOError", err)
        break

  def send(self, message):
    if self.process:
      try:
        self.process.stdin.write(message)
        self.process.stdin.flush()
      except (BrokenPipeError, OSError) as err:
        print("Failure writing to stdout", err)

