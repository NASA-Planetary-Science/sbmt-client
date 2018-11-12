#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <stdlib.h>
#include "SpiceUsr.h"
using namespace std;

struct TimeMatrix
{
    std::string utc;
    double mat[3][3];
    double pos[3];
    double vel[3];
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
		const std::string& instrumentframe,
		const std::string& spacecraftname,
		const std::string& spacecraftframe,
		const std::string& bodyname,
		const std::string& bodyframe,
		std::string& flipX,
		std::string& flipY, 
		std::string& flipZ,
        std::string& utc,
        double asteroid_to_sc[3][3],
        double position[3],
        double velocity[3])
{
    std::ifstream fin(sumfile.c_str());

    if (fin.is_open())
    {
        double et;
        double scpos[3];
        double state[6];
        double cx[3];
        double cy[3];
        double cz[3];
        double lt;

        std::string name;
        std::string dummy;
        std::string str;

        std::getline(fin, name);
        trim(name);

        std::getline(fin, utc);
        trim(utc);

        //cout << "utc is " << utc << endl;

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

        //Apply flip. Sumfiles assume increasing pixels (instrument X) 
        //is from left to right looking out the boresight, increasing 
        //lines (instrument Y) is from up to down looking out the 
        //boresight, and instrument Z is looking out of the instrument.

        if (flipX == "true" )
         {
              vminus_c(cx,cx);
	     }
        if (flipY == "true")
         {
              vminus_c(cy,cy);
	     }
        if (flipZ == "true")
         {
             vminus_c(cz,cz);
	     }

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


        /////////////////////////////////////////////////////////////////
        // The sumfiles provide the instrument_to_asteroid rotation. To
        // output the spacecraft_to_asteroid rotation to the C-Kernel,
        // chain the following rotation matrices:
        //
        // spacecraft_to_asteroid = instrument_to_asteroid * spacecraft_to_instrument
        //
        // where '*' is a matrix multiply (multiplication performed from
        // right to left).
        /////////////////////////////////////////////////////////////////

        // Get the spacecraft to instrument rotation. Time is at the spacecraft,
        // although time is irrelevant for a fix-mounted instrument.
        double sc_to_instrument[3][3];
        pxform_c(spacecraftframe.c_str(), instrumentframe.c_str(), et, sc_to_instrument);

        // Chain the rotations
        double sc_to_asteroid[3][3];
        mxm_c(instrument_to_asteroid, sc_to_instrument, sc_to_asteroid);

        // Take the inverse. MSOPCK requires this.
        invert_c(sc_to_asteroid, asteroid_to_sc);

        // Spacecraft position is in the body-fixed frame. This must be
        // specified to MKSPK.
        position[0] = scpos[0];
        position[1] = scpos[1];
        position[2] = scpos[2];

        // Sumfiles do not contain velocity. If an SPK has been loaded, use its
        // velocity in the output data. A zero velocity is assigned if the
        // spacecraft state cannot be determined.
        velocity[0] = 0;
        velocity[1] = 0;
        velocity[2] = 0;

        spkezr_c (spacecraftname.c_str(), et, bodyframe.c_str(), "NONE", bodyname.c_str(), state, &lt);
        if (!failed_c())
        {
            velocity[0] = state[3];
            velocity[1] = state[4];
            velocity[2] = state[5];
        }
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
        fout << std::scientific << tm.pos[2] << " ";
        fout << std::scientific << tm.vel[0] << " ";
        fout << std::scientific << tm.vel[1] << " ";
        fout << std::scientific << tm.vel[2] << " " << std::endl;
    }
}

std::ifstream::pos_type filesize(const char* filename)
{
  std::ifstream in(filename, std::ifstream::ate | std::ifstream::binary);
  return in.tellg(); 
}

int main(int argc, char** argv)
{
    if (argc < 11)
    {
	    std::cout << "Usage: process_sumfiles <metakernel> <sumfileList> <instrumentFrameName> <spacecraftName> <spacecraftFrameName> <bodyName> <bodyFrameName> <boolean flipX> <boolean flipY> <boolean flipZ>" << std::endl;
	    std::cout <<"E.g.:\n" << std::endl;
	    std::cout <<"process_sumfiles kernels.txt sumfilelist.txt NEAR_MSI NEAR NEAR_SC_BUS_PRIME EROS IAU_EROS false false false\n" << std::endl;
        return 1;
    }

    std::string kernelfiles = argv[1];
    std::string sumfilelist = argv[2];
    std::string instrumentframe = argv[3];
    std::string spacecraftname = argv[4];
    std::string spacecraftframe = argv[5];
    std::string bodyname = argv[6];
    std::string bodyframe = argv[7];
    std::string flipX = argv[8];
    std::string flipY = argv[9];
    std::string flipZ = argv[10];

    //Need some error handling here to check the data types of the input arguments.
    //If an incorrect type is passed in, notify the user of what was expected and
    //what they passed in.
    std::cout << std::endl;
    std::cout << "The following parameters were entered on the command line:" << std::endl;
    std::cout << "metakernel: " << kernelfiles << std::endl;
    std::cout << "sumfilelist: " << sumfilelist << std::endl;
    std::cout << "instrumentframe: " << instrumentframe << std::endl;
    std::cout << "spacecraftname: " << spacecraftname << std::endl;
    std::cout << "spacecraftframe: " << spacecraftframe << std::endl;
    std::cout << "bodyname: " << bodyname << std::endl;
    std::cout << "bodyframe: " << bodyframe << std::endl;
    std::cout << "Flip X: " << flipX << std::endl;
    std::cout << "Flip Y: " << flipY << std::endl;
    std::cout << "Flip Z: " << flipZ << std::endl;
    std::cout << std::endl;

    furnsh_c(kernelfiles.c_str());

    std::vector<std::string> sumfiles = loadFileList(sumfilelist);

    std::cout.precision(16);

    std::vector<TimeMatrix> data;

    for (unsigned int i=0; i<sumfiles.size(); ++i)
    {
        if (filesize(sumfiles[i].c_str()) <= 1153)
            continue;

        TimeMatrix tm;

	    loadSumFile(sumfiles[i], instrumentframe, spacecraftname, spacecraftframe, bodyname, bodyframe, flipX, flipY, flipZ, tm.utc, tm.mat, tm.pos, tm.vel);

        std::cout << sumfiles[i] << " " << tm.utc << std::endl;

        data.push_back(tm);
    }

    createMsopckInputDataFile(data);
    createSpkInputDataFile(data);

    return 0;
}
