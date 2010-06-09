#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <stdlib.h>
extern "C"
{
#include "SpiceUsr.h"
}


struct TimeMatrix
{
    std::string utc;
    double mat[3][3];
};


std::string& trim(std::string& s)
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
    return s;
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
		 std::string& utc,
                 double mat[3][3])
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

        std::cout << utc << std::endl;
        utc2et_c(utc.c_str(), &et);
        std::cout << et << std::endl;

	double msi_to_eros[3][3];
	msi_to_eros[0][0] = cx[0];
	msi_to_eros[0][1] = cx[1];
	msi_to_eros[0][2] = cx[2];
	msi_to_eros[1][0] = cy[0];
	msi_to_eros[1][1] = cy[1];
	msi_to_eros[1][2] = cy[2];
	msi_to_eros[2][0] = cz[0];
	msi_to_eros[2][1] = cz[1];
	msi_to_eros[2][2] = cz[2];

	double sc_bus_prime_to_msi[3][3];
	pxform_c("NEAR_SC_BUS_PRIME", "NEAR_MSI", et, sc_bus_prime_to_msi);

	double eros_to_j2000[3][3];
	pxform_c("IAU_EROS", "J2000", et, eros_to_j2000);

	double tmp[3][3];
	mxm_c(eros_to_j2000, msi_to_eros, tmp);

	double tmp2[3][3];
	mxm_c(tmp, sc_bus_prime_to_msi, tmp2);

	invert_c(tmp2, mat);
    }
    else
    {
        std::cerr << "Error: Unable to open file '" << sumfile << "'" << std::endl;
        exit(1);
    }

    fin.close();
}


void loadSpiceKernels(const std::vector<std::string>& kernelfiles)
{
    for (unsigned int i=0; i<kernelfiles.size(); ++i)
    {
        std::cout << kernelfiles[i] << std::endl;
        furnsh_c(kernelfiles[i].c_str());
    }
}


void createMsopckSetupFile()
{
    std::ofstream fout("msopcksetup");

    if (!fout.is_open())
    {
        std::cerr << "Error: Unable to open file for writing" << std::endl;
        exit(1);
    }

    fout.precision(16);

    fout << "\\begindata\n";
    
    fout << "LSK_FILE_NAME          = '/home/kahneg1/src/near/kernels/LSK/NAIF0007.TLS'\n";
    fout << "SCLK_FILE_NAME         = '/home/kahneg1/src/near/kernels/SCLK/NEAR_171.TSC'\n";
    fout << "CK_TYPE                = 3\n";
    fout << "INSTRUMENT_ID          = -93000\n";
    fout << "REFERENCE_FRAME_NAME   = 'J2000'\n";
    fout << "ANGULAR_RATE_PRESENT   = 'NO'\n";
    fout << "INPUT_TIME_TYPE        = 'UTC'\n";
    fout << "INPUT_DATA_TYPE        = 'MATRICES'\n";
    fout << "PRODUCER_ID            = 'E. Kahn, JHUAPL'\n";

    fout << "\\begintext\n";
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
        fout << std::scientific << tm.utc << " ";
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
    if (argc < 3)
    {
        std::cout << "Usage: process_sumfiles <sumfilelist> <kernelfilelist>" << std::endl;
        return 1;
    }
    
    std::string sumfilelist = argv[1];
    std::string kernelfilelist = argv[2];

    std::vector<std::string> sumfiles = loadFileList(sumfilelist);
    std::vector<std::string> kernelfiles = loadFileList(kernelfilelist);

    std::cout.precision(16);
    
    loadSpiceKernels(kernelfiles);

    createMsopckSetupFile();
    
    std::vector<TimeMatrix> data;
    
    for (unsigned int i=0; i<sumfiles.size(); ++i)
    {
        TimeMatrix tm;

        loadSumFile(sumfiles[i], tm.utc, tm.mat);
        
        data.push_back(tm);
    }

    createMsopckInputDataFile(data);    
}
