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

/*
 * Notation containing the word "To" denotes a vector. For example
 * variable AToB denotes a vector from point A to point B.
 * 
 * Notation containing the number "2" denotes a rotation. For example
 * variable A2B denotes a rotation taking vectors in reference frame A 
 * to vectors in reference frame B.
 *
 */
int loadSumFile(const std::string& sumfile,
		const std::string& instrumentframe,
		const std::string& spacecraftname,
		const std::string& spacecraftframe,
		const std::string& bodyname,
		const std::string& bodyframe,
		std::string& flipX,
		std::string& flipY,
		std::string& flipZ,
		int isJ2000,
		double scToPupil[3],
		std::string& utc,        //output variable
		double ref2sc[3][3],     //output variable, ref can be J2000 or asteroid
		double asteroidToSc[3],  //output variable,
		double velocity[3])      //output variable,
{
	std::ifstream fin(sumfile.c_str());

	if (fin.is_open())
	{
		double et;
		double asteroidToObs[3];
		double state[6];
		double cx[3];
		double cy[3];
		double cz[3];
		double cx1[3];
		double cy1[3];
		double cz1[3];
		double lt;
		double asteroid2j2000[3][3];
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

		// Sumfiles provide the observer to asteroid vector in SCOBJ.
		// We need the asteroid to observer. Note that the observer is
		// the spacecraft if pupil position is (0,0,0). Otherwise the
		// observer is the pupil (the cameral focal point).
		fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
		asteroidToObs[0] = -atof(str.c_str());
		fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
		asteroidToObs[1] = -atof(str.c_str());
		fin >> str; myReplace(str, "D", "E"); myReplace(str, "d", "E");
		asteroidToObs[2] = -atof(str.c_str());

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
		if (failed_c())
		{
			cout << "Invalid UTC in sumfile: " << utc.c_str() << "." << endl;
			//Clear the error state and return.
			reset_c();
			return -1;
		}

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

		double instrument2asteroid[3][3];
		if (flipX == "1") {
			//cout << "X unchanged" << endl;
			instrument2asteroid[0][0] = cx[0];
			instrument2asteroid[1][0] = cx[1];
			instrument2asteroid[2][0] = cx[2];
		}
		else if (flipX == "-1") {
			vminus_c(cx1, cx);
			instrument2asteroid[0][0] = cx[0];
			instrument2asteroid[1][0] = cx[1];
			instrument2asteroid[2][0] = cx[2];
		}
		else if (flipX == "2") {
			instrument2asteroid[0][1] = cx[0];
			instrument2asteroid[1][1] = cx[1];
			instrument2asteroid[2][1] = cx[2];
		}
		else if (flipX == "-2") {
			vminus_c(cx1, cx);
			instrument2asteroid[0][1] = cx[0];
			instrument2asteroid[1][1] = cx[1];
			instrument2asteroid[2][1] = cx[2];
		}
		else if (flipX == "3") {
			instrument2asteroid[0][2] = cx[0];
			instrument2asteroid[1][2] = cx[1];
			instrument2asteroid[2][2] = cx[2];
		}
		else if (flipX == "-3") {
			vminus_c(cx1, cx);
			instrument2asteroid[0][2] = cx[0];
			instrument2asteroid[1][2] = cx[1];
			instrument2asteroid[2][2] = cx[2];
		}
		else {
			std::cout << "Invalid flipX: " << flipX << ", exiting." << std::endl;
			exit(1);
		}

		if (flipY == "1") {
			instrument2asteroid[0][0] = cy[0];
			instrument2asteroid[1][0] = cy[1];
			instrument2asteroid[2][0] = cy[2];
		}
		else if (flipY == "-1") {
			vminus_c(cy1, cy);
			instrument2asteroid[0][0] = cy[0];
			instrument2asteroid[1][0] = cy[1];
			instrument2asteroid[2][0] = cy[2];
		}
		else if (flipY == "2") {
			//cout << "Y unchanged" << endl;
			instrument2asteroid[0][1] = cy[0];
			instrument2asteroid[1][1] = cy[1];
			instrument2asteroid[2][1] = cy[2];
		}
		else if (flipY == "-2") {
			vminus_c(cy1, cy);
			instrument2asteroid[0][1] = cy[0];
			instrument2asteroid[1][1] = cy[1];
			instrument2asteroid[2][1] = cy[2];
		}
		else if (flipY == "3") {
			instrument2asteroid[0][2] = cy[0];
			instrument2asteroid[1][2] = cy[1];
			instrument2asteroid[2][2] = cy[2];
		}
		else if (flipY == "-3") {
			vminus_c(cy1, cy);
			instrument2asteroid[0][2] = cy[0];
			instrument2asteroid[1][2] = cy[1];
			instrument2asteroid[2][2] = cy[2];
		}
		else {
			std::cout << "Invalid flipY: " << flipY << ", exiting." << std::endl;
			exit(1);
		}

		if (flipZ == "1") {
			instrument2asteroid[0][0] = cz[0];
			instrument2asteroid[1][0] = cz[1];
			instrument2asteroid[2][0] = cz[2];
		}
		else if (flipZ == "-1") {
			vminus_c(cz1, cz);
			instrument2asteroid[0][0] = cz[0];
			instrument2asteroid[1][0] = cz[1];
			instrument2asteroid[2][0] = cz[2];
		}
		else if (flipZ == "2") {
			instrument2asteroid[0][1] = cz[0];
			instrument2asteroid[1][1] = cz[1];
			instrument2asteroid[2][1] = cz[2];
		}
		else if (flipZ == "-2") {
			vminus_c(cz1, cz);
			instrument2asteroid[0][1] = cz[0];
			instrument2asteroid[1][1] = cz[1];
			instrument2asteroid[2][1] = cz[2];
		}
		else if (flipZ == "3") {
			//cout << "Z unchanged" << endl;
			instrument2asteroid[0][2] = cz[0];
			instrument2asteroid[1][2] = cz[1];
			instrument2asteroid[2][2] = cz[2];
		}
		else if (flipZ == "-3") {
			vminus_c(cz1, cz);
			instrument2asteroid[0][2] = cz[0];
			instrument2asteroid[1][2] = cz[1];
			instrument2asteroid[2][2] = cz[2];
		}
		else {
			std::cout << "Invalid flipZ: " << flipZ << ", exiting." << std::endl;
			exit(1);
		}

		//Check whether the flip values formed a valid rotation.
		double tolerance = 1e-8;
		if (!isrot_c(instrument2asteroid, tolerance, tolerance)) {
			std::cout << "flipX, flipY, flipZ do not define a valid rotation, see README.txt for details. Exiting." << std::endl;
			exit(1);
		}

		//Find the rotation from the asteroid frame to the J2000 frame at the
		//time the image was taken.
		if (isJ2000)
		{
			double lt, notUsed[6];
			// Get the time it takes for light to travel from the asteroid to
			// the spacecraft. Only the returned light time will be used from
			// this call, so the reference frame does not matter here, use J2000.
			spkpos_c(spacecraftname.c_str(), et, "J2000", abcorr, bodyname.c_str(), notUsed, &lt);
			if (failed_c())
			{
				cout << "ERROR: A SC SPK file must be loaded to compute the output kernels relative to J2000." << endl;
				exit(1);
			}

			// Get the orientation of the asteroid relative to J2000 at the time when
			// it was illuminated, which is one light time before the image snap time.
			pxform_c(bodyframe.c_str(), "J2000", et - lt, asteroid2j2000);
			if (failed_c())
			{
				cout << "ERROR: A body PCK file must be loaded to compute the output kernels relative to J2000." << endl;
				exit(1);
			}
		}

		/////////////////////////////////////////////////////////////////
		// The sumfiles provide the instrument2asteroid rotation. To
		// output the spacecraft2asteroid rotation to the C-Kernel,
		// chain the following rotation matrices:
		//
		// spacecraft2asteroid = instrument2asteroid * spacecraft2instrument
		//
		// where '*' is a matrix multiply (multiplication performed from
		// right to left).
		/////////////////////////////////////////////////////////////////

		// Get the spacecraft to instrument rotation. Time is at the spacecraft.
		double sc2instrument[3][3];
		pxform_c(spacecraftframe.c_str(), instrumentframe.c_str(), et, sc2instrument);

		// Chain the rotations
		double sc2asteroid[3][3];
		mxm_c(instrument2asteroid, sc2instrument, sc2asteroid);

		if (isJ2000)
		{
			// The reference frame is J2000.
			// Do the extra rotation:
			// spacecraft2j2000 = asteroid2j2000 * spacecraft2asteroid
			double sc2j2000[3][3];
			mxm_c(asteroid2j2000, sc2asteroid, sc2j2000);
			// Take the inverse. MSOPCK requires this.
			invert_c(sc2j2000, ref2sc);
		}
		else
		{
			// The reference frame is the asteroid frame.
			// Take the inverse. MSOPCK requires this.
			invert_c(sc2asteroid, ref2sc);
		}


		/////////////////////////////////////////////////////////////////
		// The sumfiles' SCOBJ is the observer to asteroid vector. We have
		// already negated it to get the asteroid to observer vector. But
		// the observer may be either the camera focal point (pupil) or the
		// spacecraft. The default observer is the spacecraft, in which case
		// the pupil vector is (0,0,0). To get the spacecraft position, the
		// following addition is performed:
		//
		// asteroidToSc = asteroidToObserver + observerToPupil
		//
		// The observerToPupil vector is specified in spacecraft coordinates,
		// while the asteroidToObserver vector is specified in asteroid
		// coordinates and must be rotated.
		/////////////////////////////////////////////////////////////////

		// Rotate observer position to the spacecraft frame.
		mtxv_c(sc2asteroid, asteroidToObs, asteroidToObs);

		// Subtract the pupil position (spacecraft to pupil vector).
		// asteroidToSc = asteroidToObs - scToPupil. Note that if the
		// spacecraft is the observer, scToPupil is the zero vector.
		vsub_c(asteroidToObs, scToPupil, asteroidToSc);

		// Rotate back to asteroid frame.
		mxv_c(sc2asteroid, asteroidToSc, asteroidToSc);

		if (isJ2000)
		{
			// Rotate spacecraft position to J2000.
			mxv_c(asteroid2j2000, asteroidToSc, asteroidToSc);
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
			reset_c();
		}

		if (isJ2000)
		{
			// Rotate velocity to J2000.
			mxv_c(asteroid2j2000, velocity, velocity);
		}
	}
	else
	{
		std::cerr << "Error: Unable to open file '" << sumfile << "'" << std::endl;
		exit(1);
	}

	fin.close();
	return 1;
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
	int isJ2000 = 0;

	// The default is the zero vector, i.e. the input sumfile SCOBJ is the
	// spacecraft to target body vector. If a pupil vector is input on the
	// command line, then it is assumed that SCOBJ is the vector from the
	// pupil location (the camera focal point) to the target body.
	// In km, in SC frame.
	double scToPupil[3] = {0,0,0};

	// Read the options first.
	int i;
	for (i = 1; i < argc; i++)
	{
		if (strcmp(argv[i], "-J2000") == 0)
		{
			isJ2000 = 1;
		}
		else if (strcmp(argv[i], "-pupil") == 0)
		{
			std::string pupil;

			pupil = argv[++i];
			scToPupil[0] = atof(pupil.c_str());
			pupil = argv[++i];
			scToPupil[1] = atof(pupil.c_str());
			pupil = argv[++i];
			scToPupil[2] = atof(pupil.c_str());
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
		std::cout <<"Usage: process_sumfiles [options] <metakernel> <sumfileList> <instrumentFrameName> <spacecraftName> <spacecraftFrameName> <bodyName> <bodyFrameName> <flipX> <flipY> <flipZ> [optional kernels]" << std::endl;
		std::cout <<"E.g.:" << std::endl;
		std::cout <<"process_sumfiles kernels.txt sumfilelist.txt NEAR_MSI NEAR NEAR_SC_BUS_PRIME EROS IAU_EROS 1 2 3\n" << std::endl;
		std::cout <<"process_sumfiles -pupil -621.34664e-6 836.26519e-6 1127.80974e-6 -J2000 kernel.mk sumfilelist_polycam.txt ORX_OCAMS_POLYCAM ORX ORX_SPACECRAFT BENNU IAU_BENNU 2 -1 3 eepPoleV03.tpc\n" << std::endl;
		return 1;
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
	std::cout << "pupil position (km), SC frame: " << scToPupil[0] << "," << scToPupil[1] << "," << scToPupil[2] << std::endl;

	for (int j = i; j < argc; j++)
	{
		std::string kernelfile = argv[j];
		trim(kernelfile);
		if (!kernelfile.empty())
		{
			cout << "kernel: " << kernelfile << endl;
		}
	}
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

	// Additional arguments found on the command line are assumed to be SPICE kernels, load last.
	for (int j = i; j < argc; j++)
	{
		std::string kernelfile = argv[j];
		trim(kernelfile);
		if (!kernelfile.empty())
		{
			furnsh_c(kernelfile.c_str());
			if (failed_c())
			{
				cout << "Error loading kernel " << kernelfile << ", skipping." << endl;
				reset_c();
			}
		}
	}

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

		int retval = loadSumFile(sumfiles[i], instrumentframe, spacecraftname, spacecraftframe, bodyname, bodyframe, flipX, flipY, flipZ, isJ2000, scToPupil, tm.utc, tm.mat, tm.pos, tm.vel);

		if (retval > 0)
		{
			data.push_back(tm);
			count++;
		}
	}

	std::cout << "Processed " << count << " of " << sumfiles.size() << " sumfiles." << std::endl << endl;

	if (isJ2000)
	{
		cout << "******************************************************************" << endl;
		cout << "* Set the reference frame in mkspksetup and msopcksetup to J2000 *" << endl;
		cout << "******************************************************************" << endl;
	}
	else
	{
		cout << "*******************************************************************************************" << endl;
		cout << "* Set the reference frame in mkspksetup and msopcksetup to the asteroid body-fixed frame. *" << endl;
		cout << "*******************************************************************************************" << endl;
	}

	createMsopckInputDataFile(data);
	createSpkInputDataFile(data);

	return 0;
}
