import sublime
import sublime_plugin

import threading
import subprocess

import os 

class AccessibleScalaHandler(sublime_plugin.EventListener):
  def __init__(self):
    here = os.path.dirname(os.path.realpath(__file__))
    cmd = ["java", "-jar", "ascala.jar"]
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

  def __del__(self):
    self.process.terminate()

  def receive_payload(self, message):
    print(message)

  def on_selection_modified_async(self, view):
    selections = view.sel()
    if selections:
      first = selections[0]
      start = str(first.begin())
      end = str(first.end())
      file = view.file_name()
      if file:
        moved = "move " + start + " " + end + " " + file + "\n"
        self.transport.send(moved)

  def on_activated_async(self, view):
    file = view.file_name()
    self.transport.send("open " + file + "\n")

class StdioTransport():
    def __init__(self, process):
        self.process = process

    def start(self, on_receive):
      self.stdout_thread = threading.Thread(target=self.read_stdout)
      self.stdout_thread.start()

    def close(self):
        self.process = None

    def read_stdout(self):
        running = True
        while running:
            running = self.process.poll() is None

            try:
                content = self.process.stdout.readline()
                print(content)

            except IOError as err:
                self.close()
                print("Failure reading stdout", err)
                break

    def send(self, message):
        if self.process:
            try:
                self.process.stdin.write(message)
                self.process.stdin.flush()
            except (BrokenPipeError, OSError) as err:
                print("Failure writing to stdout", err)
                self.close()

