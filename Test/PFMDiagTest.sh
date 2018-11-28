#!/bin/bash      
dir=$1
#echo $1

for filePath in $(find $dir -name '*.prod' -type f ); do             
	path=$filePath
	#filename=$(basename "$filePath" | sed 's!.*/!!')
	#filename="${filename%.*}"         
	dire=$(dirname $(dirname "${path}"))
	filename=$(basename " $(dirname "${path}")")
	echo $dire""/"$filename".xml,""$path
done
