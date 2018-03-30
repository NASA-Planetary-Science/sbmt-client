#! /bin/sh
status=0

if test "x$SBMTROOT" = x; then
  echo "Set SBMTROOT before running $0" >&2
  status=1
fi

if test "x$1" = x; then
  echo "Usage: $0 <mission-enum> where <mission-enum> is one of the members of SbmtMultiMissionTool.Mission" >&2
  status=1
fi

if test $status -ne 0; then
  exit $status
fi

released_mission=$1
mission_name=$2

if test $mission_name; then
  echo Setting mission name to $mission_name
  variablefile="$SBMTROOT/config/Makefiles/Variables"
  variablefileorig="$SBMTROOT/config/Makefiles/Variables-orig"
#  echo "Variable file is: " $variablefile
  mv $variablefile $variablefileorig
  cat $variablefileorig | sed "s/SNAPSHOT/$mission_name/" > $variablefile
fi

file="$SBMTROOT/src/edu/jhuapl/sbmt/client/SbmtMultiMissionTool.java"
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
