# MrFreeze

Puts files on ice...in Amazon Glacier

MrFreeze is a basic [AWS Glacier](http://aws.amazon.com/glacier/) client written in Clojure, using the AWS Java SDK. The primary goal of MrFreeze is to become a CLI tool that can be integrated into other scripts or systems. Glacier has a lot of potential for systems administrators and they should have a tool that lets them use it easily.

The secondary goal of MrFreeze is to allow me to [talk like this](http://www.youtube.com/watch?v=SRH-Ywpz1_I) at [my office](http://www.controlgroup.com) and claim it is work related. 

## Usage

MrFreeze expects that you have set the AWS_ACCESS_KEY and AWS_SECRET_KEY environment variables. You will need to set up a Glacier vault on your own (you can use the AWS console). If you don't feel like building the software, you can [grab a pre-built jar](https://github.com/drocamor/MrFreeze/downloads). 

Uploading a file to AWS Glacier:

```java -jar mrfreeze.jar -A upload -v <vault> -f <file>```

The upload process will provide you with a very long string that is the archive ID. You will need it to download a file from AWS Glacier:

```java -jar mrfreeze.jar -A download -v <vault> -f <file> -r <archive id>```

Describe a Glacier vault:

```java -jar mrfreeze.jar -A describe -v <vault>```

Requent an inventory a vault:

```java -jar mrfreeze.jar -A inventory -v <vault>```


## Notes about Glacier and MrFreeze

Some people are describing Glacier as a cheaper version of S3. My experience so far shows that this is not the case. Yes, they both store files, but that's about where the similarities end.

Operations in Glacier move at a glacial pace. A request to restore a vaulted file takes about 4 hours to complete. When it comes to MrFreeze, this means that the program will run for a few hours (seemingly doing nothing) while it waits to download your file. I don't think this is ideal, but it was the easiest way to hit the ground running with Glacier. 

## License

Copyright © 2012 David Rocamora

Distributed under the Eclipse Public License, the same as Clojure.
