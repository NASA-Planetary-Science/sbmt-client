#! /bin/sh
status=0

if test "x$SBMTROOT" = x; then
  echo "Set SBMTROOT before running $0" >&2
  status=1
fi

if test "x$1" = x; then
  echo "Usage: $0 <mission-enum> where <mission-enum> is one of the members of SmallBodyMappingTool.Mission" >&2
  status=1
fi

if test $status -ne 0; then
  exit $status
fi

released_mission=$1

file="$SBMTROOT/src/edu/jhuapl/sbmt/client/SmallBodyMappingTool.java"
if test ! -f $file; then
  echo "Cannot find file $file" >&2
  exit 1
fi

string='Mission[ \t][ \t]*RELEASED_MISSION[ \t]*=[ \t]*';

if test `grep -c "$string" $file` -eq 0; then
  echo "Could not match RELEASED_MISSION field in file $file" >&2
  exit 1
fi

sed -e "s:\($string\).*:\1Mission.$released_mission;:" $file > $file-new
status=$?
if test $status -ne 0; then
  echo "Error occurred while editing file $file" >&2
  exit $status
fi

mv $file $file-orig
mv $file-new $file
