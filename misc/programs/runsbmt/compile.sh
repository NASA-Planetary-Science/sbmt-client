#/bin/sh

# Use MinGW to build the program with this command. Then sign the
# binary with the signtool program.

x86_64-w64-mingw32-g++.exe runsbmt.cpp -O2 -static -mwindows -mms-bitfields -o runsbmt.exe

# To sign, run from the Visual Studio command prompt:
# signtool sign /a runsbmt.exe
