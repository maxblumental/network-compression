usage="Usage: $ ./convert_to_tflite <saved_model_dir> <out_file_name>"

if [ -z $1 ]
then 
  echo "provide directory with the saved model"
  echo $usage
  exit 1
fi

if [ -z $2 ]
then 
  echo "provide a name for the output file"
  echo $usage
  exit 1
fi

tflite_convert --output_file=$2 --saved_model_dir=$1
