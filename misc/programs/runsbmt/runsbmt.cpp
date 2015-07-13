#include <stdlib.h>
#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <string>
#include <sstream>
#include <fstream>

using namespace std;

static void runCommand( TCHAR *command )
{
    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory( &si, sizeof(si) );
    si.cb = sizeof(si);
    ZeroMemory( &pi, sizeof(pi) );

    // Start the child process.
    if( !CreateProcess( NULL,           // No module name (use command line)
                        command,        // Command line
                        NULL,           // Process handle not inheritable
                        NULL,           // Thread handle not inheritable
                        FALSE,          // Set handle inheritance to FALSE
                        0,              // No creation flags
                        NULL,           // Use parent's environment block
                        NULL,           // Use parent's starting directory
                        &si,            // Pointer to STARTUPINFO structure
                        &pi )           // Pointer to PROCESS_INFORMATION structure
        )
    {
        printf( "CreateProcess failed (%d).\n", GetLastError() );
        return;
    }

    // Do not wait for child. Exit right away.
    WaitForSingleObject( pi.hProcess, 0 );

    // Close process and thread handles.
    CloseHandle( pi.hProcess );
    CloseHandle( pi.hThread );
}

static bool exists(const string& file)
{
    ifstream fin(file.c_str());
    if (fin.good())
    {
        fin.close();
        return true;
    }

    return false;
}

template <typename T>
string numberToString ( T Number )
{
  ostringstream ss;
  ss << Number;
  return ss.str();
}

// Get memory size in kilobytes
static size_t getMemorySize()
{
  MEMORYSTATUSEX status;
  status.dwLength = sizeof(status);
  GlobalMemoryStatusEx( &status );
  return (size_t)(status.ullTotalPhys/1024);
}

int main(int argc, char *argv[])
{
    string command = ".\\jre\\bin\\javaw.exe";

    size_t memory = getMemorySize();
    command += " -Xmx" + numberToString(memory) + "K";
    command += " -Djava.library.path=lib/win64 -Dsun.java2d.noddraw=true -jar lib/near.jar";

    for (int i=1; i<argc; ++i)
        command += string(" ") + argv[i];

    // change PATH environmental variable
    string path = getenv( "PATH" );
    string newpathenv = "PATH=";
    newpathenv += path;
    newpathenv += ";.\\lib\\win64";
    _putenv( newpathenv.c_str() );

    runCommand((TCHAR*)command.c_str());

    return 0;
}
