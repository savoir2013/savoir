for loop in `ls  ./$1/*.jar`
do 
   echo $loop
   ./signjar.sh $loop
done
