# Beacon

My first version of my attempt to make an SOS app. The app sends out SMS messages to users whom you add from your contact list to let them know you are under distress. It sends these messages out when you have gps on and off. If the gps is on, it will send gps coordinates. If the network and gps is on, it will take a google maps url, shorten the url with Google's Url shortener, and send it in the SMS message. if both nework and gps is off, it will still send messages but without gps coordinates. This is the app in it's best form before I decided to rewrite it. It also comes with a map that should show your location but only to the user since the app communicates via SMS. I wasn't happy with this so I'm rewriting this with a server backend so that the app can show user's on a map within the app.

Take a look at the initial commit date on this app and the compare it with all the other apps in the app store. What's on master is the point where I decided put the app in a git repo.
