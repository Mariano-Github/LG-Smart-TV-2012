/**
 *  LG Smart TV Device Type
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Modified Mar 2021 By Mariano Colmenarejo
 *  Perform Secuence of Remote codes for Enter in Power Save mode: Screen OFF and Mute (clicking Pause)
 *  Perform Secuence for Power save Off and Mute Off (Clicking Play)
 *  Perfor TV Power Off click Off button. For restart TV ON, click ON on your remote control
 */
 
metadata {
    definition (name: "LG-TV 2012+ Control Power Save", namespace: "smartthings", author: "Samlalor mod by MCC")
    
    {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"
        
        attribute "sessionId", "string"
	}
    preferences {
        input("televisionIp", "string", title:"Television IP Address", description: "Television's IP address", required: true, displayDuringSetup: false)
        input("pairingKey", "string", title:"Pairing Key", description: "Pairing key", required: true, displayDuringSetup: false)
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
				attributeState("paused", label:"Paused")
				attributeState("playing", label:"Playing")
				attributeState("stopped", label:"Stopped")
			}
			tileAttribute("device.status", key: "SECONDARY_CONTROL") {
				attributeState("paused", label:"Paused", action:"music Player.play", nextState: "playing")
				attributeState("playing", label:"Play", action:"music Player.pause", nextState: "paused")
				attributeState("stopped", label:"Stopped", action:"music Player.play", nextState: "playing")
			}
            tileAttribute("device.status", key: "PREVIOUS_TRACK") {
				attributeState("status", action:"music Player.previousTrack", defaultState: true)
			}
			tileAttribute("device.status", key: "NEXT_TRACK") {
				attributeState("status", action:"music Player.nextTrack", defaultState: true)
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState("level", action:"music Player.setLevel")
			}
			tileAttribute ("device.mute", key: "MEDIA_MUTED") {
				attributeState("unmuted", action:"music Player.mute", nextState: "muted")
				attributeState("muted", action:"music Player.unmute", nextState: "unmuted")
			}
			tileAttribute("device.trackDescription", key: "MARQUEE") {
				attributeState("trackDescription", label:"${currentValue}", defaultState: true)
			}
		}

		main "mediaMulti"
		details(["mediaMulti"])
	}
}

def installed() {
 log.debug "Installed"
 
 //variables state:
 //state.counter: Counter for secuence control (0 = pause ini secuence, 10= play ini secuence, 15= refresh ini, -2= end secuence)
 //state.Id: save sessionId for control TV response to key pairing sent in sessionIdCommand()
 //state.StatusTV: last TV Status, playing, stopped or paused
 //state.backup: for save state.counter value before refresh execute (state.counter=15)
 //state.Track: Messages in track media tile 
 
 //Messages for track secuences
 state.tracks = [
 "TV Playing. Click Pause(||) to Mute & TV Screen OFF or Power(') for TV off  ", //state.currentTrack =0
 "TV Paused. Click Play(>) to Unmute & TV Screen ON or Power(') for TV off  ", //state.currentTrack =1
 "TV Power OFF, Click Power (') to refresh TV status ON/OFF  ", //state.currentTrack =2
 "Wait!... Sending 'Mute & TV Screen OFF' Commands  ", //state.currentTrack =3
 "Wait!... Sending 'UnMute & TV Screen ON' Commands  ",//state.currentTrack =4
 "Wait!... Sending 'TV Power OFF' Commands  ",//state.currentTrack =5
 "Updating TV status ON/OFF  "] //state.currentTrack =6
 state.currentTrack = 0
 sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
 
 state.counter = -2
 unschedule()
 sendEvent(name: "switch", value: "on")
 sendEvent(name: "mute", value: "unmuted")
 sendEvent(name: "status", value: "playing")
 state.StatusTV = "playing"

 //*** Every 5 minutes refrech device status if TV is power Off/On
 runEvery5Minutes(refresh)
}

def update() {
 refresh()
}

//parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"

    if (description == "updated") 
    {
    	sendEvent(name:'refresh', displayed:false)
    }
    else
    {
    	parseHttpResult(description)
        log.debug "vuelvo a parse desde parseHttpResult"
        log.debug "Parse-1: state.backup= $state.backup; state.counter= $state.counter; state.Id= $state.Id"
    }
  if (state.counter == 11) { 
   sendEvent(name: "switch", value: "on")
   sendEvent(name: "mute", value: "unmuted")
   sendEvent(name: "status", value: "playing")
   state.StatusTV = "playing"
   state.currentTrack = 0
   sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
  }
  if (state.counter == 5) {
   sendEvent(name: "switch", value: "on")
   sendEvent(name: "status", value: "paused")
   state.StatusTV = "paused"
   state.currentTrack = 1
   sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
  }
  
  if (state.counter == 5 ||state.counter == 12) {state.counter = -2}
  if (state.counter < 0) {
   state.Id= "end"
   log.debug "state.StatusTV = $state.StatusTV"
  }
  log.debug "Parse-2: state.backup= $state.backup; state.counter= $state.counter; state.Id= $state.Id"
  
  if (state.counter == 15) {
    if (state.StatusTV == "stopped" && state.Id != null) {
       log.debug "Do TV play secuence"
       state.counter = 10
    }
    if (state.Id == null) {
     log.debug "TV Off Line or Power Off"
     state.currentTrack = 2
     sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
     state.counter = -2 
     sendEvent(name: "switch", value: "off")
     sendEvent(name: "mute", value: "unmuted")
     sendEvent(name: "status", value: "stopped")
     state.StatusTV = "stopped"
    } else {
      log.debug " TV response state.Id= $state.Id. 'TV Could be Power save or Power On'"
      log.debug "state.StatusTV = $state.StatusTV"
      sendEvent(name: "switch", value: "on")
      if (state.counter != 10) {state.counter = state.backup}
     }
  }
  nextCode()
}

// ***** Do secuence TV screen off and on *******
def nextCode() {
  log.debug "En Next----state.counter= $state.counter"

  if (state.counter == 0 ||state.counter == 10) { runIn(1, 'mute1',[overwrite: true]) }
  if (state.counter == 1) { runIn(7, 'save',[overwrite: true]) }
  if (state.counter == 2 || state.counter == 3) { runIn(1, 'up',[overwrite: true]) }
  if (state.counter == 4) { runIn(1, 'ok',[overwrite: true]) }
  if (state.counter == 11) { runIn(2, 'mute1',[overwrite: true]) }
}

def on() {
  log.debug "Detect if TV is ON"
  if (device.currentValue("switch")== "off" ) {
   log.debug "Switch= off"
   state.currentTrack = 6
   sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])

   refresh()
  }
}

//*******  turn TV OFF *****
def off () {
   log.debug "turn TV OFF"
   sendEvent(name: "mute", value: "unmuted")
   sendEvent(name: "switch", value: "off")
   sendEvent(name: "status", value: "stopped")
   state.StatusTV = "stopped"
   state.currentTrack = 2
   sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
   return sendCommand(1)
}

def play() { 
    log.debug "Executing 'Power Screen On + Mute Off'"
    state.counter = 10
    state.currentTrack = 4
    sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])

    return refresh()
    //sendHubCommand(new physicalgraph.device.HubAction ("wake on lan ${CC2D8C924621}", physicalgraph.device.Protocol.LAN, null, [:]))
}

def pause() {	
    log.debug "Executing 'Power Save: screen Off + Mute On'"
     state.currentTrack = 3
   sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])

    state.counter = 0
    return refresh()
}

def setLevel() {
   log.debug "level"
}

def previousTrack() {
  log.debug "previousTrack"
}

def nextTrack() {
  log.debug "nextTrack"
}

def channelUp() {
	log.debug "Executing 'channelUp'"
	return sendCommand(27)
}

def channelDown() {
	log.debug "Executing 'channelDown'"
	return sendCommand(28)
}

// handle commands

def refresh() {
    log.debug "Executing 'refresh' to Detect TV Power Off"
    if (state.counter == 0) {
     sendEvent(name: "mute", value: "muted")
    }
    //detect TV OFF
    if (state.Id == "") {
     log.debug "state.Id= null detect TV OFF"
     sendEvent(name: "status", value: "stopped")
     sendEvent(name: "mute", value: "unmuted")
     sendEvent(name: "switch", value: "off")
     state.StatusTV = "stopped"
     state.currentTrack = 2
     sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
   }
    state.backup = state.counter
    state.counter = 15
    log.debug "state.backup= $state.backup; state.counter= $state.counter"
    log.debug "state.StatusTV = $state.StatusTV"
    return sessionIdCommand()
}
def unmute () {
  log.debug "unmute"
  //state.counter = 15
  //sendEvent(name: "mute", value: "unmuted")
  //return sendCommand(26)
}
def mute() {
	log.debug "Executing 'mute'"
    //state.counter = 15
    //sendEvent(name: "mute", value: "muted")
    //return sendCommand(26)
}

//****** save energy send code
def save() {
 	log.debug "Executing 'save'"
    return sendCommand(409)
}
//***** Execute mute command
def mute1() {
 return sendCommand(26)
}

def externalInput() {
    log.debug "47"
	return sendCommand(47)
}

def back() {
    log.debug "23"
	return sendCommand(23)
}

def up() {
	log.debug "Executing 'up'" 
    return sendCommand(12)
}

def down() {
    log.debug "Executing 'down'" 
    return sendCommand(13)
}

def left() {
    log.debug "14"
	return sendCommand(14)
}

def right() {
    log.debug "15"
	return sendCommand(15)
}

def myApps() {
	log.debug "417"
    return sendCommand(417)
}

def ok() {
    log.debug "Executing 'ok' for power save mode" 
    return sendCommand(20)
}

def home() {
   log.debug "home"
   return sendCommand(21)
}

def sendCommand(cmd) {   
    sessionIdCommand()
    log.debug "vuelvo a sendCommand(cmd) de sessionIdCommand()"
    tvCommand(cmd)
    log.debug "vuelvo a sendCommand(cmd) de tvCommand(cmd)"
    state.counter = state.counter + 1
}

// **** Send TV pairing Key
def sessionIdCommand() {
    state.Id = ""
    def commandText = "<?xml version=\"1.0\" encoding=\"utf-8\"?><auth><type>AuthReq</type><value>$pairingKey</value></auth>"       
    def httpRequest = [
      	method:		"POST",
        path: 		"/roap/api/auth",
        body:		"$commandText",
        headers:	[
        				HOST:			"$televisionIp:8080",
                        "Content-Type":	"application/atom+xml",
                    ]
	]
    
    try 
    {
    	def hubAction = new physicalgraph.device.HubAction(httpRequest)
        log.debug "hub action: $hubAction"
        //return hubAction
        sendHubCommand(hubAction)
    }
    catch (Exception e) 
    {
		log.debug "Hit Exception $e on $hubAction"
	}
}

// **** Send TV remote code command
def tvCommand(cmd) {
    def commandText = "<?xml version=\"1.0\" encoding=\"utf-8\"?><command><type>HandleKeyInput</type><value>${cmd}</value></command>"

    def httpRequest = [
      	method:		"POST",
        path: 		"/udap/api/command",
        body:		"$commandText",
        headers:	[
        				HOST:			"$televisionIp:8080",
                        "Content-Type":	"application/atom+xml",
                    ]
	]
    
    try 
    {
    	def hubAction = new physicalgraph.device.HubAction(httpRequest)
        log.debug "hub action: $hubAction"
    	//return hubAction
        sendHubCommand(hubAction)
    }
    catch (Exception e) 
    {
		log.debug "Hit Exception $e on $hubAction"
	}
}

def appCommand() {
	log.debug "********Reached App Command"
    def commandText = "<?xml version=\"1.0\" encoding=\"utf-8\"?><envelope><api type=\"command\"><name>AppExecute</name><auid>1</auid><appname>Netflix</appname><contentId>1</contentId></api></envelope>"

    def httpRequest = [
      	method:		"POST",
        path: 		"/udap/api/command",
        body:		"$commandText",
        headers:	[
        				HOST:			"$televisionIp:8080",
                        "Content-Type":	"application/atom+xml",
                    ]
	]
    
    try 
    {
    	def hubAction = new physicalgraph.device.HubAction(httpRequest)
        log.debug "hub action: $hubAction"
    	//return hubAction
        sendHubCommand(hubAction)
    }
    catch (Exception e) 
    {
		log.debug "Hit Exception $e on $hubAction"
	}
}

private parseHttpResult (output) {
	def headers = ""
	def parsedHeaders = ""
    
    def msg = parseLanMessage(output)

    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)

	log.debug "headers: $headerMap, status: $status, body: $body, data: $json"
  
    if (status == 200){
    	parseSessionId(body)
    }
    else if (status == 401){
    	log.debug "Unauthorized - clearing session value"
    	sendEvent(name:'sessionId', value:'', displayed:false)
        sendEvent(name:'refresh', displayed:false)
    }
}

def String parseSessionId(bodyString) {
	def sessionId = ""
	def body = new XmlSlurper().parseText(bodyString)
  	sessionId = body.session.text()
    state.Id = sessionId

	if (sessionId != null && sessionId != "")
  	{
  		sendEvent(name:'sessionId', value:sessionId, displayed:false)
  		log.debug "session id: $sessionId"
    }
    log.debug "session id-2: $sessionId"
}

private parseHttpHeaders(String headers) {
	def lines = headers.readLines()
	def status = lines[0].split()

	def result = [
	  protocol: status[0],
	  status: status[1].toInteger(),
	  reason: status[2]
	]

	if (result.status == 200) {
		log.debug "Authentication successful! : $status"
	}
    else
    {
    	log.debug "Authentication Unsuccessful: $status"
    }

	return result
}

private def delayHubAction(ms) {
    log.debug("delayHubAction(${ms})")
    return new physicalgraph.device.HubAction("delay ${ms}")
}
