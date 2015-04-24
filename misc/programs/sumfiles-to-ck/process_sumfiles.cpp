/*** EXPORT CONTROLLED (12/2013) ***/

#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <stdlib.h>
#include "SpiceUsr.h"


struct TimeMatrix
{
    std::string utc;
    double mat[3][3];
    double pos[3];
};


// Remove initial and trailing whitespace from string. Modifies string in-place
void trim(std::string& s)
{
    const std::size_t si = s.find_first_not_of(" \t");
    if (si != std::string::npos)
    {
        const std::size_t ei = s.find_last_not_of(" \t");
        const std::size_t l = (ei == std::string::npos ? ei : ei - si + 1);
        s = s.substr(si, l);
    }
    else
    {
        s = "";
    }
}


std::vector<std::string> loadFileList(const std::string& filelist)
{
    std::ifstream fin(filelist.c_str());

    std::vector<std::string> files;

    if (fin.is_open())
    {
        std::string line;
        while (std::getline(fin, line))
            files.push_back(line);
    }
    else
    {
        std::cerr << "Error: Unable to open file '" << filelist << "'" << std::endl;
        exit(1);
    }

    return files;
}

void myReplace(std::string& str, const std::string& oldStr, const std::string& newStr){
  size_t pos = 0;
  while((pos = str.find(oldStr, pos)) != std::string::npos){
    str.replace(pos, oldStr.length(), newStr);
    pos += newStr.length();
  }
}


void loadSumFile(const std::string& sumfile,
                 const std::string& asteroidframe,
                 std::string& utc,
                 double j2000_to_instrument[3][3],
                 double position[3])
{
    std::ifstream fin(sumfile.c_str());

    if (fin.is_open())
    {
        double et;
        double scpos[3];
        double cx[3];
        double cy[3];
        double cz[3];

        std::string name;
        std::string dummy;
        std::string str;

        std::getline(fin, name);
        trim(name);

        std::getline(fin, utc);
        trim(utc);

        // Replace spaces with dashes in the utc string
        std::replace(utc.begin(), utc.end(), ' ', '-');

        for (int i=0; i<7; ++i) fin >> dummy;
        for (int i=0; i<5; ++i) fin >> dummy;
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        scpos[0] = -atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        scpos[1] = -atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        scpos[2] = -atof(str.c_str());

        fin >> dummy;

        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cx[0] = atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cx[1] = atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cx[2] = atof(str.c_str());

        fin >> dummy;

        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cy[0] = atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cy[1] = atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cy[2] = atof(str.c_str());

        fin >> dummy;

        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cz[0] = atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cz[1] = atof(str.c_str());
        fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
        cz[2] = atof(str.c_str());

        fin >> dummy;

        for (int i=0; i<4; ++i) fin >> dummy;

        utc2et_c(utc.c_str(), &et);

        vhat_c(cx, cx);
        vhat_c(cy, cy);
        vhat_c(cz, cz);

        double instrument_to_asteroid[3][3];
        instrument_to_asteroid[0][0] = cx[0];
        instrument_to_asteroid[1][0] = cx[1];
        instrument_to_asteroid[2][0] = cx[2];
        instrument_to_asteroid[0][1] = cy[0];
        instrument_to_asteroid[1][1] = cy[1];
        instrument_to_asteroid[2][1] = cy[2];
        instrument_to_asteroid[0][2] = cz[0];
        instrument_to_asteroid[1][2] = cz[1];
        instrument_to_asteroid[2][2] = cz[2];

        double asteroid_to_j2000[3][3];
        pxform_c(asteroidframe.c_str(), "J2000", et, asteroid_to_j2000);

        double instrument_to_j2000[3][3];
        mxm_c(instrument_to_asteroid, asteroid_to_j2000, instrument_to_j2000);

        invert_c(instrument_to_j2000, j2000_to_instrument);

        position[0] = scpos[0];
        position[1] = scpos[1];
        position[2] = scpos[2];
    }
    else
    {
        std::cerr << "Error: Unable to open file '" << sumfile << "'" << std::endl;
        exit(1);
    }

    fin.close();
}


void createMsopckInputDataFile(const std::vector<TimeMatrix>& data)
{
    std::ofstream fout("msopckinputdata");

    if (!fout.is_open())
    {
        std::cerr << "Error: Unable to open file for writing" << std::endl;
        exit(1);
    }

    fout.precision(16);

    for (unsigned int i=0; i<data.size(); ++i)
    {
        const TimeMatrix& tm = data[i];
        fout << tm.utc << " ";
        fout << std::scientific << tm.mat[0][0] << " ";
        fout << std::scientific << tm.mat[0][1] << " ";
        fout << std::scientific << tm.mat[0][2] << " ";
        fout << std::scientific << tm.mat[1][0] << " ";
        fout << std::scientific << tm.mat[1][1] << " ";
        fout << std::scientific << tm.mat[1][2] << " ";
        fout << std::scientific << tm.mat[2][0] << " ";
        fout << std::scientific << tm.mat[2][1] << " ";
        fout << std::scientific << tm.mat[2][2] << std::endl;
    }
}

void createSpkInputDataFile(const std::vector<TimeMatrix>& data)
{
    std::ofstream fout("mkspkinputdata");

    if (!fout.is_open())
    {
        std::cerr << "Error: Unable to open file for writing" << std::endl;
        exit(1);
    }

    fout.precision(16);

    for (unsigned int i=0; i<data.size(); ++i)
    {
        const TimeMatrix& tm = data[i];
        fout << tm.utc << " ";
        fout << std::scientific << tm.pos[0] << " ";
        fout << std::scientific << tm.pos[1] << " ";
        fout << std::scientific << tm.pos[2] << " 0.0 0.0 0.0" << std::endl;
    }
}

std::ifstream::pos_type filesize(const char* filename)
{
  std::ifstream in(filename, std::ifstream::ate | std::ifstream::binary);
  return in.tellg(); 
}

int main(int argc, char** argv)
{
    if (argc < 4)
    {
        std::cout << "Usage: process_sumfiles <kernelfiles> <sumfilelist> <name-of-asteroid-frame>" << std::endl;
        return 1;
    }

    std::string kernelfiles = argv[1];
    std::string sumfilelist = argv[2];
    std::string asteroidframe = argv[3];

    furnsh_c(kernelfiles.c_str());

    std::vector<std::string> sumfiles = loadFileList(sumfilelist);

    std::cout.precision(16);

    std::vector<TimeMatrix> data;

    for (unsigned int i=0; i<sumfiles.size(); ++i)
    {
        if (filesize(sumfiles[i].c_str()) <= 1153)
            continue;

        TimeMatrix tm;

        loadSumFile(sumfiles[i], asteroidframe, tm.utc, tm.mat, tm.pos);

        std::cout << sumfiles[i] << " " << tm.utc << std::endl;

        data.push_back(tm);
    }

    createMsopckInputDataFile(data);
    createSpkInputDataFile(data);

    return 0;
}
