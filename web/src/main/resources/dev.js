require("./sass/app-main.scss");


var meSpeak = require("node_modules/mespeak");
var config = require("node_modules/mespeak/src/mespeak_config.json")
var voice = require("node_modules/mespeak/voices/en/en-us.json")
meSpeak.loadConfig(config)
meSpeak.loadVoice(voice)

window.meSpeak = meSpeak 
