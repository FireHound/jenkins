import groovy.json.JsonSlurper

String getDay() { ['date', '+%u'].execute().text.trim()}
String getDevices() { ['curl', '-s', 'https://raw.githubusercontent.com/FireHound/jenkins/o8.1/build-targets.json'].execute().text }

def jsonParse(def json) { new groovy.json.JsonSlurperClassic().parseText(json) }

node {
  try {
    def json = jsonParse(getDevices())
	def dow = getDay()
    for(int i = 0; i < json.size(); i++) {
		if (json[i].dow == dow){
			echo "${json[i].device} is scheduled to be built today."
			build job: 'builder', parameters: [
			string(name: 'DEVICE', value: (json[i].device == null) ? "lolwut" : json[i].device),
			string(name: 'BUILD_TYPE', value: (json[i].build_type == null) ? "userdebug" : json[i].build_type)
			], propagate: false, wait: false
			sleep 5
		}
    }
  } catch (e) {
    currentBuild.result = "FAILED"
    throw e
  }
}
