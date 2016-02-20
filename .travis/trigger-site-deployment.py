import os
import urllib2
import json

def request(url, data, token = None):
  req = urllib2.Request(url, data)
  req.add_header('User-Agent', 'MyClient/1.0.0')
  req.add_header('Accept', 'application/vnd.travis-ci.2+json')
  req.add_header('Content-Type', 'application/json; charset=UTF-8')
  if token:
    req.add_header('Travis-API-Version', '3')
    req.add_header('Authorization', 'token ' + travis_token)
  p = urllib2.urlopen(req)
  return json.loads(p.read())

travis_token = request('https://api.travis-ci.org/auth/github', '{"github_token":"' + os.environ['GH_TOKEN'] + '"}')['access_token']
print(request('https://api.travis-ci.org/repo/jacoco%2Fwww.eclemma.org/requests', '{ "request": { "branch": "master", "message": "New JaCoCo snapshot" } }', travis_token))
