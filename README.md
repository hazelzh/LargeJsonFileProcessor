**Overview**
The application applies the changes from the changes file to the mixtape.json data, and produce output.json.
The application can read large mixtape.json files with jackson stream API.
To speed up, the application load entire change.json file into memory, this way, 
the processing speed is almost the same as copying the mixtape.json file to output.json file.

**Input and Output**
Input: mixtape.json file and changes.json file
Output: output.json file

**How to run**
step 1:
Since it's a java application, the jdk is needed to be install. 
To check if jdk is installed, use this command: java -version
the response looks like:
java version "15.0.1" 2020-10-20
Java(TM) SE Runtime Environment (build 15.0.1+9-18)
Java HotSpot(TM) 64-Bit Server VM (build 15.0.1+9-18, mixed mode, sharing)

step 2:
in the test directory, there are some the case and a jar file: the playlist-jar-with-dependencies.jar, changes.json, mixtape.json

step 3:
You can run the jar directly, use the command:
java -jar playlist-jar-with-dependencies.jar param1 param2 param3

param1: the file path of mixtape.json 
param2: the file path of changes.json 
param3: the output directory

for example:
java -jar playlist-jar-with-dependencies.jar ./mixtape.json ./changes.json ./

**How to scale**
If the changes file can be loaded into memory, the bottleneck would be disk io(read from mixtape.json
and write to output.json), we could split mixtape.json into smaller files;
If the changes file is too large that can not be loaded into memory, we could split both mixtape.json and 
the changes file into smaller files.
Then we could process these smaller files in parallel.

How to split:
We could name each smaller file as a chunk,
each chunk maintains the playlists whose id from n * s to (n + 1) * s - 1, where n is chunk file number, 
s is the playlists num that each chunk maintains
For example:
Each chunk maintains 1000 playlists(s = 1000), then
changes file: 
changes_0.json(maintaining the changes of playlists whose id from 0 to 999)
changes_1.json(maintaining the changes of playlists whose id from 1000 to 1999) 
...
changes_n.json(maintaining the changes of playlists whose id from n*1000 to n*1000 + 999) 

Similarly, we split mixtape.json to multiple playlists chunks. Since we just change playlists, 
so we could just split playlists.
mixtape_0.json (maintaining playlists whose id from 0 to 999)
mixtape_1.json (maintaining playlists whose id from 1000 to 1999)
...
mixtape_n.json(maintaining  playlists whose id from 1000 * n to 1000 * n + 999) 
