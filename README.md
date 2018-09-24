# Burp-Scripter
Burp extension to inject script output into HTTP parameters

## Purpose
This extension was made during a web auditing with a poor Captcha solution. The application used a pair of values (Captcha and Captcha key) to validate the login process. I used a custom script to automate the Captcha resolution, but there was no way I knew of to use this script with Burp.

The final version aims to cover a wider range of scenarios. To do so, any kind of script that could be invoked from the shell can be used, and any number of parameters could be set up to inject values into them. Please refer to the [Usage](https://github.com/3riatarka/Burp-Scripter#usage) section to know exactly how it is meant to be used.

## Installation
For the time being, this extension only works in Linux environments (not tested on Mac, but it should work too). To get it working in Windows, you need to modify the command execution fragment on [the extension source file](src/burp/BurpExtender.java)(lines 248~260).

To install the extension, download [the .jar file](build/Burp-Scripter.jar), and add it manually as `Java` type extension in the Extensions tab inside Burp Extender.

## Usage
Once installed, a new tab should be present in your Burp setup called `Scripter`. It has three input and one output fields. 

The input consists of: 
  - The absolute path to the script to be executed. This script should __ONLY__ output a comma separated list of parameter values, in the same order as the parameter names present in the third input field.
  - The list of HTTP parameter names to pass to the user script (comma separated list without spaces). If you want to pass any parameter value from the original HTTP request (for example, user name and password to calculate a hash), add the parameter names, and they will be passed as command line parameters to your script in the same order as specified. This field can be empty.
  - The HTTP parameter name list. This list has to be a comma separated list (without spaces) of the parameter names that will have its values replaced by those outputted from the script. Remember, they need to be in the same order as the ones that the script outputs.

Also, there are some buttons to help some (and get me working with Swing design, to be honest):
  - Test script: this one executes the supplied script and prints its output into the text field below. This way you can be sure of the amount and order of values it prints. Also, if there is some kind of errors (permissions, file not found, etc), the Java stack trace should be printed instead. If your script need input parameters, there is no way for now to test it, sorry (you'll have to test it from CLI).
  - Save: this button saves the input fields to the Burp database, so it should be as you left them in case the extension gets unloaded or the program closes. It is also useful if you have to change the configuration and re-enable the extension. 
  - Clear form: self-explanatory, removes any input from the previous fields. Just to annoy you if you press it by mistake.
  - Clear output: removes all previous output text from the text field.

To modify the HTTP messages, you need to add a new Session Handling Rule in the Sessions tab in Project options. Configure the scope, and add a new rule that invokes an extension handler, and select `Execute custom script`. Then the tools configured will pass the message to Burp Scripter and replace the parameters specified.

There is some error and debug level printing on the Ouput tab of the extension (visible in the __Extender__ tab).

### To do list:
 - [x] First working version
 - [x] Added user script parameters
 - [ ] Fix __Test script__ button to work with script parameters
 - [ ] Add usage examples (images, video)
 - [ ] Add support to Windows environments (and test it)

# DISCLAIMER
I am no developer, and this is my first useful Java project. I tested it in my environment, and works well, but it may have unexpected behaviors if something changes.

Also, keep in mind that there is an `exec()` inside it, use it **ONLY** with scripts that you made or reviewed deeply, any code inside it could potentially be executed with the privileges that the user that started Burp had. In the end, it is almost all this extension does: execute a user supplied script.

