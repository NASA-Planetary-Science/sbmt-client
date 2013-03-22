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
    PointCloud<PointNormal>::Ptr cloud_smoothed_normals (new PointCloud<PointNormal> ());

    io::loadPCDFile (argv[1], *cloud_smoothed_normals);
    std::cout << "Finished loading" << std::endl;


    Poisson<PointNormal> poisson;
    poisson.setDepth (9);
    poisson.setInputCloud(cloud_smoothed_normals);
    PolygonMesh mesh;
    poisson.reconstruct (mesh);

    std::cout << "Finished poisson" << std::endl;

    io::saveVTKFile (argv[2], mesh);

    return 0;
}
