# OdooDigitalPersona
Simple Java App for connect Digital Persona Device with Odoo

## Usage

In order to use the Java App OdooDigitalPersona you need a Digital Persona Device (like U.are.U 4500) and the module [odoo_digital_personal](https://github.com/codize-app/odoo_digital_persona) installed in your Odoo Server.

## Build the App

Add the Java libraries (.jar) for connection with Digital Persona Device on folder lib/java. The Libraries can be found on oficial Digital Persona SDK, these are:

`dpjavapos`
`dpuareu`
`jpos113`
`xercesImpl-2.6.2`
`xmlParserAPIs`

Then, edit the file OdooDigitalPersona.java. Search the variable odooUrl and replace it content by your Odoo Server URL.
Execute the file `build.bat` for build your own App.
Then execute file `run.bat` for execute the App (OdooDigitalPersona.jar)

---
Contact: info@codize.ar

© Codize
