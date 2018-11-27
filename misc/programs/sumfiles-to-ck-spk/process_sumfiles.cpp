#include <fstream>
#include <iostream>
#include <vector>
#include <cstring>
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
        {
            trim(line);
            files.push_back(line);
        }
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
          int isJ2000SPK,
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
        double cx1[3];
        double cy1[3];
        double cz1[3];
        double lt;
	double asteroid_to_j2000[3][3];
        const char* abcorr = "LT+S";

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
        vhat_c(cx, cx1);
        vhat_c(cy, cy1);
        vhat_c(cz, cz1);

        //Apply flip. Sumfiles assume increasing pixels (instrument X) 
        //is from left to right looking out the boresight, increasing 
        //lines (instrument Y) is from up to down looking out the 
        //boresight, and instrument Z is looking out of the instrument.

          double instrument_to_asteroid[3][3];
          if (flipX == "1") {
	       //cout << "X unchanged" << endl;
               instrument_to_asteroid[0][0] = cx[0];
               instrument_to_asteroid[1][0] = cx[1];
               instrument_to_asteroid[2][0] = cx[2];
          }
          else if (flipX == "-1") {
               vminus_c(cx1, cx);
               instrument_to_asteroid[0][0] = cx[0];
               instrument_to_asteroid[1][0] = cx[1];
               instrument_to_asteroid[2][0] = cx[2];
          }
          else if (flipX == "2") {
               instrument_to_asteroid[0][1] = cx[0];
               instrument_to_asteroid[1][1] = cx[1];
               instrument_to_asteroid[2][1] = cx[2];
          }
          else if (flipX == "-2") {
               vminus_c(cx1, cx);
               instrument_to_asteroid[0][1] = cx[0];
               instrument_to_asteroid[1][1] = cx[1];
               instrument_to_asteroid[2][1] = cx[2];
          }
          else if (flipX == "3") {
               instrument_to_asteroid[0][2] = cx[0];
               instrument_to_asteroid[1][2] = cx[1];
               instrument_to_asteroid[2][2] = cx[2];
          }
          else if (flipX == "-3") {
               vminus_c(cx1, cx);
               instrument_to_asteroid[0][2] = cx[0];
               instrument_to_asteroid[1][2] = cx[1];
               instrument_to_asteroid[2][2] = cx[2];
          }
          else {
               std::cout << "Invalid flipX: " << flipX << ", exiting." << std::endl;
               exit(1);
          }

          if (flipY == "1") {
               instrument_to_asteroid[0][0] = cy[0];
               instrument_to_asteroid[1][0] = cy[1];
               instrument_to_asteroid[2][0] = cy[2];
          }
          else if (flipY == "-1") {
               vminus_c(cy1, cy);
               instrument_to_asteroid[0][0] = cy[0];
               instrument_to_asteroid[1][0] = cy[1];
               instrument_to_asteroid[2][0] = cy[2];
          }
          else if (flipY == "2") {
	       //cout << "Y unchanged" << endl;
               instrument_to_asteroid[0][1] = cy[0];
               instrument_to_asteroid[1][1] = cy[1];
               instrument_to_asteroid[2][1] = cy[2];
          }
          else if (flipY == "-2") {
               vminus_c(cy1, cy);
               instrument_to_asteroid[0][1] = cy[0];
               instrument_to_asteroid[1][1] = cy[1];
               instrument_to_asteroid[2][1] = cy[2];
          }
          else if (flipY == "3") {
               instrument_to_asteroid[0][2] = cy[0];
               instrument_to_asteroid[1][2] = cy[1];
               instrument_to_asteroid[2][2] = cy[2];
          }
          else if (flipY == "-3") {
               vminus_c(cy1, cy);
               instrument_to_asteroid[0][2] = cy[0];
               instrument_to_asteroid[1][2] = cy[1];
               instrument_to_asteroid[2][2] = cy[2];
          }
          else {
               std::cout << "Invalid flipY: " << flipY << ", exiting." << std::endl;
               exit(1);
          }

          if (flipZ == "1") {
               instrument_to_asteroid[0][0] = cz[0];
               instrument_to_asteroid[1][0] = cz[1];
               instrument_to_asteroid[2][0] = cz[2];
          }
          else if (flipZ == "-1") {
               vminus_c(cz1, cz);
               instrument_to_asteroid[0][0] = cz[0];
               instrument_to_asteroid[1][0] = cz[1];
               instrument_to_asteroid[2][0] = cz[2];
          }
          else if (flipZ == "2") {
               instrument_to_asteroid[0][1] = cz[0];
               instrument_to_asteroid[1][1] = cz[1];
               instrument_to_asteroid[2][1] = cz[2];
          }
          else if (flipZ == "-2") {
               vminus_c(cz1, cz);
               instrument_to_asteroid[0][1] = cz[0];
               instrument_to_asteroid[1][1] = cz[1];
               instrument_to_asteroid[2][1] = cz[2];
          }
          else if (flipZ == "3") {
               //cout << "Z unchanged" << endl;
               instrument_to_asteroid[0][2] = cz[0];
               instrument_to_asteroid[1][2] = cz[1];
               instrument_to_asteroid[2][2] = cz[2];
          }
          else if (flipZ == "-3") {
               vminus_c(cz1, cz);
               instrument_to_asteroid[0][2] = cz[0];
               instrument_to_asteroid[1][2] = cz[1];
               instrument_to_asteroid[2][2] = cz[2];
          }
          else {
               std::cout << "Invalid flipZ: " << flipZ << ", exiting." << std::endl;
               exit(1);
          }

          //Check whether the flip values formed a valid rotation.
          double tolerance = 1e-8;
          if (!isrot_c(instrument_to_asteroid, tolerance, tolerance)) {
               std::cout << "flipX, flipY, flipZ do not define a valid rotation, see README.txt for details. Exiting." << std::endl;
               exit(1);
          }

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

	if (isJ2000SPK)
	  {
	    double lt, notUsed[6];
	    // Get the time it takes for light to travel from the asteroid to
	    // the spacecraft. Only the returned light time will be used from
	    // this call, so the reference frame does not matter here, use J2000.
	    spkpos_c(spacecraftname.c_str(), et, "J2000", abcorr, bodyname.c_str(), notUsed, &lt);
	    if (failed_c())
	    {
		cout << "ERROR: An SPK file must be loaded to compute J2000 state." << endl;
            }
	
	    // Get the orientation of the asteroid relative to J2000 at the time when
            // it was illuminated, which is one light time before the image snap time.
	    pxform_c(bodyframe.c_str(), "J2000", et - lt, asteroid_to_j2000);
	    if (failed_c())
            {
		cout << "ERROR: A PCK file must be loaded to compute J2000 orientation." << endl;
            }
	  }

        // Spacecraft position is in the body-fixed frame. This must be
        // specified to MKSPK.
        position[0] = scpos[0];
        position[1] = scpos[1];
        position[2] = scpos[2];

	if (isJ2000SPK)
	{
	    // Rotate position to J2000.
	    mxv_c(asteroid_to_j2000, position, position);
	}

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
        else
	{
	  // This is not necessarily a problem. If no SPK is loaded, zero velocity 
          // in the output file is the expected behavior.
	  cout << "No SPK loaded. Zero velocity will be written to mkspkinputdata." << endl;
	}

	if (isJ2000SPK)
	{
	    // Rotate velocity to J2000.
	    mxv_c(asteroid_to_j2000, velocity, velocity);
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
    int isJ2000SPK = 0;

    // Read the options first.
    int i;
    for (i = 1; i < argc; i++)
    {
        if (strcmp(argv[i], "-J2000SPK") == 0)
        {
            isJ2000SPK = 1;
        }            
        else
        {
            // We've encountered something that is not an option, must be at the args
            break;
        }
    }

    // There must be numRequiredArgs arguments remaining after the options.
    // Otherwise abort.
    int numberRequiredArgs = 10;
    if (argc - i < numberRequiredArgs)
    {
       std::cout <<"\nThis program generates CK and SPK from sumfiles. " << std::endl;
       std::cout <<"See README.txt in the distribution for detailed usage. " << std::endl;
       std::cout <<"Usage: process_sumfiles [optional flag] <metakernel> <sumfileList> <instrumentFrameName> <spacecraftName> <spacecraftFrameName> <bodyName> <bodyFrameName> <flipX> <flipY> <flipZ>" << std::endl;
       std::cout <<"E.g.:" << std::endl;
       std::cout <<"process_sumfiles -J2000SPK kernels.txt sumfilelist.txt NEAR_MSI NEAR NEAR_SC_BUS_PRIME EROS IAU_EROS 1 2 3\n" << std::endl;
       return 1;
    }

    // Notify user that any additional arguments found on the command line are ignored.
    for (int j = numberRequiredArgs + 1; j <= argc - i; j++)
    {
       cout << "Additional argument " << argv[j] << " ignored." << endl;
    }

    std::string kernelfiles = argv[i++];
    std::string sumfilelist = argv[i++];
    std::string instrumentframe = argv[i++];
    std::string spacecraftname = argv[i++];
    std::string spacecraftframe = argv[i++];
    std::string bodyname = argv[i++];
    std::string bodyframe = argv[i++];
    std::string flipX = argv[i++];
    std::string flipY = argv[i++];
    std::string flipZ = argv[i++];

    // SPICE error handling.
    erract_c ( (char *)"SET", 0, (char *)"RETURN" );
    
    // Need some error handling here to check the data types of the input arguments.
    // If an incorrect type is passed in, notify the user of what was expected and
    // what they passed in.
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

    // Check for valid flip values.
    if (flipX != "1" && flipX != "-1" && flipX != "2" && flipX != "-2" && flipX != "3" && flipX != "-3")
    {
         std::cout << "Invalid flipX: " << flipX << std::endl;
         std::cout << "Allowed values: {-1, 1, -2, 2, -3, 3}. See README.txt for details." << std::endl;
         return 1;
    }
    if (flipY != "1" && flipY != "-1" && flipY != "2" && flipY != "-2" && flipY != "3" && flipY != "-3")
    {
         std::cout << "Invalid flipY: " << flipY << std::endl;
         std::cout << "Allowed values: {-1, 1, -2, 2, -3, 3}. See README.txt for details." << std::endl;
         return 1;
    }
    if (flipZ != "1" && flipZ != "-1" && flipZ != "2" && flipZ != "-2" && flipZ != "3" && flipZ != "-3")
    {
         std::cout << "Invalid flipZ: " << flipZ << std::endl;
         std::cout << "Allowed values: {-1, 1, -2, 2, -3, 3}. See README.txt for details." << std::endl;
         return 1;
    }

    furnsh_c(kernelfiles.c_str());

    std::vector<std::string> sumfiles = loadFileList(sumfilelist);

    std::cout.precision(16);

    std::vector<TimeMatrix> data;

    int count = 0;
    for (unsigned int i=0; i<sumfiles.size(); ++i)
    {
      std::cout << i+1 << ") " << sumfiles[i].c_str() << endl;

      //        if (filesize(sumfiles[i].c_str()) <= 1153)
      //	{
      //	  cout << "Invalid file size " << filesize(sumfiles[i].c_str()) << ". Sumfile not processed. " << endl;
      //            continue;
      //        }

        TimeMatrix tm;

        loadSumFile(sumfiles[i], instrumentframe, spacecraftname, spacecraftframe, bodyname, bodyframe, flipX, flipY, flipZ, isJ2000SPK, tm.utc, tm.mat, tm.pos, tm.vel);

        data.push_back(tm);

        count++;
    }

    std::cout << "Processed " << count << " of " << sumfiles.size() << " sumfiles." << std::endl << endl;

    if (isJ2000SPK)
    {
      cout << "**************************************************" << endl;
      cout << "* Set the reference frame in mkspksetup to J2000 *" << endl;
      cout << "**************************************************" << endl;
    }
    else
    {
      cout << "***************************************************************************" << endl;
      cout << "* Set the reference frame in mkspksetup to the asteroid body-fixed frame. *" << endl;
      cout << "***************************************************************************" << endl;
    }

    createMsopckInputDataFile(data);
    createSpkInputDataFile(data);

    return 0;
}
