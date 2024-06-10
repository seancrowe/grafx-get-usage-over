# grafx-get-usage-over
Simple hacky script that uses [babashka](https://github.com/babashka/http-client) that gets the total number of clients who are over thier usage.

Steps to use:
1. Install bb
2. Modify script with your authentication header
3. Run script and wait

Sometimes you will get a 500 ruining your script run... oh well that is just the API 😉 and you gotta do it again.

If you get a 401, you authentication bearer token is bad.
