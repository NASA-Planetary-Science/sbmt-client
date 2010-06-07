#include <fstream>
#include <iostream>
#include <vector>
#include <string>


std::vector<std::string> loadSumFileList(std::string sumfilelist)
{

}


void loadSumFile(std::string sumfile,
		 double cx[3],
		 double cy[3],
		 double cz[3])
{

}


int main(int argc, char** argv)
{
  std::string sumfilelist = argv[1];
  std::vector<std::string> sumfiles = loadSumFileList(sumfilelist);
  
  for (unsigned int i=0; i<sumfiles.size(); ++i)
    {
      
    }
}
