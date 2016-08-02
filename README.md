"# TestExecutionReport"

Author: Kevin Lewis

Purpose: Add-on for JIRA Cloud Zephyr Test Case Management, creates graphs based off of various filters

Code: Java, html, javascript

Project Structure: using Maven
- POM file location: .../TestExecutionReport/pom.xml

Run commands:
- To run the automation of the export of the xml files use: clean test -e
- To run the post to the Firebase database use: exec: java@post -e
# -e is not required, just provides a more elaborate stack trace if errors occur


Timing is still off when waiting for search bar element to appear during the automation due to the responsiveness of the JIRA website
