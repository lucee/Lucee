<!--
{
  "title": "Encryption/Decryption with RSA public and private keys",
  "id": "encryption_decryption",
  "related": [
    "function-decrypt",
    "function-encrypt",
    "function-generatersakeys"
  ],
  "categories": [
    "crypto"
  ],
  "description": "This document explains about Encryption/Decryption with public and private keys with simple examples.",
  "menuTitle": "Public and Private keys",
  "keywords": [
    "Encryption",
    "Decryption",
    "RSA",
    "Public key",
    "Private key",
    "Lucee"
  ]
}
-->
## Encryption/Decryption ##

This document explains about Encryption/Decryption with public and private keys with simple examples.

Encryption/Decryption is a new functionality in Lucee 5.3. We have a new way to encrypt/decrypt string values. First we start with keys. In this case, there are two keys:

* Private key to encrypt
* Public key to decrypt

### Example 1: ###

```luceescript
//index.cfm
directory action="list" directory=getDirectoryFromPath(getCurrentTemplatePath()) filter="example*.cfm" name="dir";
loop query=dir {
	echo('<a href="#dir.name#">#dir.name#</a><br>');
}
```

```luceescript
key=generateRSAKeys();
dump(key)
```

This function generates RSA keys. Execute the example code above in the browser and a struct is returned containing the two keys: a private key and a public key. So, we can create these keys, and store them somewhere for later use.

### Example 2: ###

```luceescript
key=generateRSAKeys();
raw="Hi, Hello !!!";
enc=encrypt(raw,key.private,"rsa");
dump(enc);
```

We now create RSA keys using the [[function-generatersakeys]] function, and then use the key to encrypt using the [[function-encrypt]] function. The encrypt() function has some arguments. It has `key.private` which defines the key as the private key, and `rsa` indicates use of the RSA encryption algorithm. Then run the dump in the browser and we see the encrypted string for your input string.

### Example 3: ###

```luceescript
key=generateRSAKeys();
raw="Hi, Hello !!!";
enc=encrypt(raw,key.private,"rsa");
dec=decrypt(enc,key.public,"rsa");
dump(dec);
```

This is a full detailed example of encrypt/decrypt functions. We create a key and we encrypt with the private key. Then we [[function-decrypt]] with the public key. Then run the dump in the browser and we see the original string returned as expected.

### Footnotes ###

Here you can see these details in the video also:

[Encryption/Decryption with public and private keys](https://www.youtube.com/watch?v=2fgfq-3nWfk)