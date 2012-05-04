#include "platemodel.h"
#include "util.h"
#include <fstream>
#include <string>
#include <vector>
#include <iostream>
#include <stdlib.h>

using namespace std;

Platemodel::Platemodel()
{
}

void Platemodel::load(std::string filename)
{
    ifstream fin(filename.c_str());
    if (fin.is_open())
    {
        int numPoints = -1;
        int numPlates = -1;
        string line;

        getline(fin, line);
        trim(line);
        vector<string> tokens = split(line);
        if (tokens.size() >= 1)
            numPoints = atoi(tokens[0].c_str());
        if (tokens.size() == 2)
            numPlates = atoi(tokens[1].c_str());
        if (tokens.size() < 1 || tokens.size() > 2)
        {
            cerr << "Error: File format incorrect " << endl;
            exit(1);
        }

        Vertex x;
        for (int i=0; i<numPoints; ++i)
        {
            getline(fin, line);
            trim(line);
            tokens = split(line);
            if (tokens.size() != 4)
            {
                cerr << "Error: File format incorrect" << endl;
                exit(1);
            }
            x.point[0] = atof(tokens[1].c_str());
            x.point[1] = atof(tokens[2].c_str());
            x.point[2] = atof(tokens[3].c_str());
            vertices.push_back(x);
        }

        if (numPlates < 0)
        {
            getline(fin, line);
            trim(line);
            vector<string> tokens = split(line);
            if (tokens.size() != 1)
            {
                cerr << "Error: File format incorrect" << endl;
                exit(1);
            }
            numPlates = atoi(tokens[0].c_str());
        }

        Plate p;
        for (int i=0; i<numPlates; ++i)
        {
            getline(fin, line);
            trim(line);
            tokens = split(line);
            if (tokens.size() != 4)
            {
                cerr << "Error: File format incorrect" << endl;
                exit(1);
            }
            p.cell[0] = atoi(tokens[1].c_str())-1;
            p.cell[1] = atoi(tokens[2].c_str())-1;
            p.cell[2] = atoi(tokens[3].c_str())-1;

            ComputeNormal(
                        vertices[p.cell[0]].point,
                        vertices[p.cell[1]].point,
                        vertices[p.cell[2]].point,
                        p.normal
                        );

            plates.push_back(p);
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }}
