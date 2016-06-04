# Network
A simple and easy to use HTTP and HTTPS Java library

## License
This repository is licensed under the MIT license. Please refer to the LICENSE.md file for more information.

## Download
You can either add the source directory to your project, or download the jar [here](https://github.com/JochemKuijpers/Network/blob/master/jar/network-20160604.jar).

## Examples

### Downloading a binary file
Downloading a binary file only takes a few lines of code.

```java
Connection con = new HttpConnection("example.com");

byte[] fileContent = con.get("path/to/image.png");

FileOutputStream out = new FileOutputStream("image.png");
out.write(fileContent);
out.close();
```

### Sending a form via a POST request

Note that the get method also allows you to set fields.
```java
Connection con = new HttpConnection("example.com");

Map<String,String> fields = new HashMap<String,String>();
fields.put("name", "John Doe");
fields.put("age", "42");
fields.put("email", "j.doe@example.com");

byte[] response = con.post("register.php", fields);

if (con.getStatus().contains("200")) {
	System.out.println("Success!");
} else {
	System.out.println("Response status: " + con.getStatus());
}
```

### Convert the response byte array to a string
You can easily convert the response array to a String or other object. 
Note: JSONObject is not part of this small library and is only present to display the potential use of this code.

```java
// ...
byte[] response = con.get("");
String responseText = new String(response);
JSONObject jsonData = new JSONObject(responseText);
// etc.
```
	
### HTTPS requests
If you want to use HTTPS instead of plaintext HTTP, just use a different Connection object:

```java
Connection con = new HttpsConnection("example.com");
// ...
```

### Uploading files and form fields via HTTPS
Here's how you can upload two files using a POST multipart request over HTTPS. Besides two files, it also uploads two form fields `first_name` and `last_name`:

```java
InputFile screenshotFile = new InputFile("screenshot.png", 
		"image/png",
		Files.readAllBytes(Paths.get("screenshot.png")));
InputFile documentFile = new InputFile("document.docx", 
		"application/octet-stream",
		Files.readAllBytes(Paths.get("document.docx")));

Connection con = new HttpsConnection("example.com");

Map<String, String> fields = new HashMap<String, String>();
fields.put("first_name", "Jane");
fields.put("last_name", "Doe");

Map<String, InputFile> files = new HashMap<String, InputFile>();
files.put("screenshot", screenshotFile);
files.put("document", documentFile);

byte[] response = con.post("upload.php", fields, files);

System.out.println(con.getStatus());		// HTTP/1.1 200 OK
System.out.println(new String(response))	// Thanks for uploading your files!
```

# Issues?

Please let me know by creating an issue or by contacting me via [my personal website](http://jochemkuijpers.nl/contact). Thanks!

If you can fix it yourself, please try to stick to the original coding style and motivate your changes in the pull request.
