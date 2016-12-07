# NUSPACKER #

### What is NUSPacker ###

NUSPacker is an open-source tool to pack files into an installable format for the WiiU. To install the created packages, you need to run a patched iosu (see: https://github.com/dimok789/iosuhax)

### How do I use it? ###

#### Setup ####
To use this, you'll a folder which contains a "code", "content" and "meta" folder. Make sure that the content folder is not empty.  
  
*Optional:*  
To save you some typing you can also create a text file "encryptKeyWith" which contains the common key as hexstring (32 characters). This will be used to encrypt the encryption key.   
#### Pack files ####
To pack files, you can use these arguments:
```
-in             ; is the dir where you have your decrypted data. Make this pointing to the root folder with the folder code,content and meta.
-out            ; Where the installable package will be saves

(optional! will be parsed from app.xml if missing)
-tID            ; titleId of this package. Will be saved in the TMD and provided as 00050000XXXXXXXX
-OSVersion      ; target OS version
-appType        ; app type
-skipXMLParsing ; disables the app.xml parsing

(optional! defaults values will be used if missing (or loaded from external file))
-encryptionKey  ; the key that is used to encrypt the package
-encryptKeyWith ; the key that is used to encrypt the encryption key

```
  
In most cases the following command will be totally fine. (make sure to have a valid app.xml)
```
java -jar NUSPacker.jar -in "inputDir" -out "outputDir"
```
Without common key stored in file.
```
java -jar NUSPacker.jar -in "inputDir" -out "outputDir" -encryptKeyWith 12345678123456781234567812345678
```
