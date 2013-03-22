#include <pcl/common/common.h>
#include <pcl/io/pcd_io.h>
#include <pcl/features/normal_3d_omp.h>
#include <pcl/surface/mls.h>
#include <pcl/surface/poisson.h>
#include <pcl/io/vtk_io.h>

using namespace pcl;
int main(int argc, char **argv)
{
    if (argc != 3)
    {
        PCL_ERROR ("Syntax: %s input.pcd output.ply\n", argv[0]);
        return -1;
    }
    PointCloud<PointXYZ>::Ptr cloud (new PointCloud<PointXYZ> ());
    PointCloud<PointXYZ>::Ptr cloud_smoothed (new PointCloud<PointXYZ> ());

    io::loadPCDFile (argv[1], *cloud);
    std::cout << "Finished loading" << std::endl;

    MovingLeastSquares<PointXYZ, PointXYZ> mls;
    mls.setInputCloud (cloud);
    mls.setSearchRadius (0.01);
    mls.setPolynomialFit (true);
    mls.setPolynomialOrder (2);
    mls.setUpsamplingMethod (MovingLeastSquares<PointXYZ, PointXYZ>::SAMPLE_LOCAL_PLANE);
    mls.setUpsamplingRadius (0.005);
    mls.setUpsamplingStepSize (0.003);
    mls.process (*cloud_smoothed);
    std::cout << "Finished mls" << std::endl;

    io::savePCDFileASCII (argv[2], *cloud_smoothed);
    return 0;
}
