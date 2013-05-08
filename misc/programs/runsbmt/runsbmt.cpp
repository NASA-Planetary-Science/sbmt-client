#include <stdlib.h>
#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <string>
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

int main(int argc, char *argv[])
{
    // See if a jre exists in the current folder or a few standard locations

    string command;

    // See if there's a jre in the current folder
    if (command.empty())
    {
        string javaFile = ".\\jre7\\bin\\javaw.exe";
        if (exists(javaFile))
        {
            command = javaFile;
        }
    }

    // Try a default java 7 location
    if (command.empty())
    {
        string javaFile = "C:\\Program Files\\Java\\jre7\\bin\\javaw.exe";
        if (exists(javaFile))
        {
            command = "\"" + javaFile + "\"";
        }
    }

    // Try a default java 6 location
    if (command.empty())
    {
        string javaFile = "C:\\Program Files\\Java\\jre6\\bin\\javaw.exe";
        if (exists(javaFile))
        {
            command = "\"" + javaFile + "\"";
        }
    }

    // if command is still empty, just use what's on the path
    if (command.empty())
    {
        command = "javaw";
    }

    command += " -Djava.library.path=lib/win64 -Dsun.java2d.noddraw=true -jar lib/near.jar";

    // change PATH environmental variable
    string path = getenv( "PATH" );
    string newpathenv = "PATH=";
    newpathenv += path;
    newpathenv += ";.\\lib\\win64";
    _putenv( newpathenv.c_str() );

    runCommand((TCHAR*)command.c_str());

    return 0;
}
