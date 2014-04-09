cmake_minimum_required (VERSION 2.6)
project (INFO_FILE_CREATOR)

set(SPICE_DIR /project/nearsdc/software/spice/cspice/ CACHE PATH "Path to Spice")

include_directories(${SPICE_DIR}/include)
link_directories(${SPICE_DIR}/lib)

add_executable(create_info_files create_info_files.cpp)
target_link_libraries(create_info_files cspice)
