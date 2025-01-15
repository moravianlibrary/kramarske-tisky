if [ $# -ne 3 ] ; then
  echo "Usage: $0 input_file_tiff ouput_file_jp2_archive_copy ouput_file_jp2_user_copy"
else
  INPUT_FILE_TIFF=$1
  OUTPUT_FILE_JP2_ARCHIVE_COPY=$2
  OUTPUT_FILE_JP2_USER_COPY=$3

  #check that input file exists
  if [ ! -f $INPUT_FILE_TIFF ]; then
    echo "File $INPUT_FILE_TIFF does not exist"
    exit 1
  fi
  #delete output files if they exist
  if [ -f OUTPUT_FILE_JP2_ARCHIVE_COPY ]; then
    echo "File OUTPUT_FILE_JP2_ARCHIVE_COPY exists, deleting"
    rm OUTPUT_FILE_JP2_ARCHIVE_COPY
  fi
  if [ -f OUTPUT_FILE_JP2_USER_COPY ]; then
    echo "File OUTPUT_FILE_JP2_USER_COPY exists, deleting"
    rm OUTPUT_FILE_JP2_USER_COPY
  fi

  # tiff to jp2 with kakadu
  #this is lossy compression, for more options see
  #http://kakadusoftware.com/wp-content/uploads/2014/06/Usage_Examples1.txt
  #https://www.ndk.cz/standardy-digitalizace/standardy-pro-obrazova-data
  #kdu_compress -i ${INPUT_FILE_TIFF} -o ${OUTPUT_FILE} -rate 2.5  Clayers=1 Clevels=7 "Cprecincts={256,256}" "Corder=RPCL" "Cblk={64,64}" Cuse_sop=yes -jp2_space sLUM
  #kdu_compress -i ${INPUT_FILE_TIFF} -o ${OUTPUT_FILE} -rate -  Clayers=12 Clevels=5 "Stiles={1024,1024}" "Cprecincts={256,256},{128,128}" "Corder=RPCL" "Cblk={64,64}" Cuse_sop=yes -jp2_space sLUM
  
  # archive copy quality
  kdu_compress -i ${INPUT_FILE_TIFF} -o ${OUTPUT_FILE_JP2_ARCHIVE_COPY} "Cblk={64,64}" Corder=RPCL "Stiles={4096,4096}" "Cprecincts={256,256},{256,256},{128,128}" ORGtparts=R Creversible=yes Clayers=1 Clevels=5 "Cmodes={BYPASS}" Cuse_sop=yes Cuse_eph=yes
  # master copy 1:8 quality
  #kdu_compress -i ${INPUT_FILE_TIFF} -o ${OUTPUT_FILE_JP2_MASTER_COPY} "Cblk={64,64}" Corder=RPCL "Stiles={1024,1024}" "Cprecincts={256,256},{256,256},{128,128}" ORGtparts=R -rate 3 Clayers=12 Clevels=5 "Cmodes={BYPASS}"
  # master copy 1:10 quality
  kdu_compress -i ${INPUT_FILE_TIFF} -o ${OUTPUT_FILE_JP2_USER_COPY} "Cblk={64,64}" Corder=RPCL "Stiles={1024,1024}" "Cprecincts={256,256},{256,256},{128,128}" ORGtparts=R -rate 2.4 Clayers=12 Clevels=5 "Cmodes={BYPASS}"
  # master copy 1:20 quality
  #kdu_compress -i ${INPUT_FILE_TIFF} -o ${OUTPUT_FILE} "Cblk={64,64}" Corder=RPCL "Stiles={1024,1024}" "Cprecincts={256,256},{256,256},{128,128}" ORGtparts=R -rate 1.2 Clayers=12 Clevels=5 "Cmodes={BYPASS}"
  # master copy 1:30 quality
  #kdu_compress -i ${INPUT_FILE_TIFF} -o ${OUTPUT_FILE} "Cblk={64,64}" Corder=RPCL "Stiles={1024,1024}" "Cprecincts={256,256},{256,256},{128,128}" ORGtparts=R -rate 0.8 Clayers=12 Clevels=5 "Cmodes={BYPASS}"

fi  