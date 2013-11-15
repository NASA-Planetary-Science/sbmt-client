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


void loadSumFile(const std::string& sumfile,
                 const std::string& asteroidframe,
                 std::string& utc,
                 double j2000_to_instrument[3][3])
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

        std::getline(fin, name);
        trim(name);

        std::getline(fin, utc);
        trim(utc);

        // Replace spaces with dashes in the utc string
        std::replace(utc.begin(), utc.end(), ' ', '-');

        for (int i=0; i<7; ++i) fin >> dummy;
        for (int i=0; i<5; ++i) fin >> dummy;
        fin >> scpos[0];
        fin >> scpos[1];
        fin >> scpos[2];
        fin >> dummy;
        fin >> cx[0];
        fin >> cx[1];
        fin >> cx[2];
        fin >> dummy;
        fin >> cy[0];
        fin >> cy[1];
        fin >> cy[2];
        fin >> dummy;
        fin >> cz[0];
        fin >> cz[1];
        fin >> cz[2];
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
        TimeMatrix tm;

        loadSumFile(sumfiles[i], asteroidframe, tm.utc, tm.mat);

        data.push_back(tm);
    }

    createMsopckInputDataFile(data);

    return 0;
}
