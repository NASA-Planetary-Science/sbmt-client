#include <stdio.h>
#include <stdlib.h>
#include <set>
#include <string>

int main(int argc, char** argv)
{
    if (argc < 3)
    {
        printf("Usage: %s <input-file> <output-file>\n", argv[0]);
        return 1;
    }
    
    const char* inputfilename = argv[1];
    FILE *fin = fopen(inputfilename, "r");
    if (fin == NULL)
    {
        printf("Could not open %s\n", inputfilename);
        exit(1);
    }

    const char* outfilename = argv[2];
    FILE *fout = fopen(outfilename, "w");
    if (fout == NULL)
    {
        printf("Could not open %s\n", outfilename);
        exit(1);
    }
        
    char line[1024];
    char utc[128];
    int met;
    std::set<int> allMets;
    std::set<std::string> allUtcs;
        
    while ( fgets ( line, sizeof line, fin ) != NULL ) /* read a line */
    {
        sscanf(line, "%d %s", &met, utc);

        if (allMets.find(met) == allMets.end() &&
            allUtcs.find(std::string(utc)) == allUtcs.end())
        {
            fprintf(fout, "%s", line);
        }

        allMets.insert(met);
        allUtcs.insert(std::string(utc));
    }

    fclose ( fin );
    fclose ( fout );
}
