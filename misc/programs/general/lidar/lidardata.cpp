#include "lidardata.h"
#include "util.h"
#include "SpiceUsr.h"
#include <fstream>
#include <iostream>
#include <math.h>
#include <stdlib.h>


LidarData::LidarData()
{
}

Track LidarData::loadTrack(const string& filename,
                                bool convertToJ2000,
                                const string& bodyName,
                                double startTime,
                                double stopTime)
{
    ifstream fin(filename.c_str());

    Track referenceTrajectory;
    char utc[24];

    if (fin.is_open())
    {
        string line;
        while (getline(fin, line))
        {
            Point p;
            vector<string> tokens = split(line);

            if (bodyName == "ITOKAWA")
            {
                string time = tokens[1].c_str();
                utc2et_c(time.c_str(), &p.time);

                if (startTime < stopTime)
                {
                    if (p.time < startTime || p.time >= stopTime)
                        continue;
                }

                p.range = atof(tokens[2].c_str());

                if (tokens.size() > 3)
                {
                    p.scpos[0] = atof(tokens[3].c_str());
                    p.scpos[1] = atof(tokens[4].c_str());
                    p.scpos[2] = atof(tokens[5].c_str());
                    p.targetpos[0] = atof(tokens[6].c_str());
                    p.targetpos[1] = atof(tokens[7].c_str());
                    p.targetpos[2] = atof(tokens[8].c_str());
                    p.intersectpos[0] = atof(tokens[9].c_str());
                    p.intersectpos[1] = atof(tokens[10].c_str());
                    p.intersectpos[2] = atof(tokens[11].c_str());

                    p.boredir[0] = p.targetpos[0] - p.scpos[0];
                    p.boredir[1] = p.targetpos[1] - p.scpos[1];
                    p.boredir[2] = p.targetpos[2] - p.scpos[2];
                    vhat_c(p.boredir, p.boredir);

                    if (convertToJ2000)
                    {
                        // transform to J2000
                        const char* frame = "IAU_ITOKAWA";
                        const char* ref = "J2000";
                        double i2bmat[3][3];
                        pxform_c(frame, ref, p.time, i2bmat);
                        mxv_c(i2bmat, p.scpos, p.scpos);
                    }
                }
            }
            else if (bodyName == "EROS")
            {
                // The first 2 lines have as the first character either an 'L' or 'l'.
                // Ignore them
                if (line[0] == 'L' || line[0] == 'l')
                    continue;

                int noise = atoi(tokens[7].c_str());
                if (noise == 1)
                    continue;

                utc2et_c(utc, &p.time);

                if (startTime < stopTime)
                {
                    if (p.time < startTime || p.time >= stopTime)
                        continue;
                }

                p.range = atof(tokens[5].c_str()) / 1000.0;

                double sclon = atof(tokens[8].c_str())*M_PI/180.0;
                double sclat = atof(tokens[9].c_str())*M_PI/180.0;
                double scrdst = atof(tokens[10].c_str())/1000.0;
                latrec_c(scrdst, sclon, sclat, p.scpos);

                p.targetpos[0] = atof(tokens[14].c_str());
                p.targetpos[1] = atof(tokens[15].c_str());
                p.targetpos[2] = atof(tokens[16].c_str());

                p.boredir[0] = p.targetpos[0] - p.scpos[0];
                p.boredir[1] = p.targetpos[1] - p.scpos[1];
                p.boredir[2] = p.targetpos[2] - p.scpos[2];
                vhat_c(p.boredir, p.boredir);

                // transform to J2000
                if (convertToJ2000)
                {
                    const char* frame = "IAU_EROS";
                    const char* ref = "J2000";
                    double i2bmat[3][3];
                    pxform_c(frame, ref, p.time, i2bmat);
                    mxv_c(i2bmat, p.scpos, p.scpos);
                }
            }

            referenceTrajectory.push_back(p);
        }

        fin.close();
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }

    return referenceTrajectory;
}

void LidarData::saveTrack(const string &filename,
                          const Track& track,
                          bool convertFromJ2000,
                          const string& bodyName)
{
    ofstream fout(filename.c_str());

    if (fout.is_open())
    {
        for (unsigned int i = 0; i<track.size(); ++i)
        {
            const Point& p = track[i];

            if (bodyName == "ITOKAWA")
            {
                char strTime[32];
                et2utc_c(p.time, "ISOC", 3, 32, strTime);

                if(convertFromJ2000)
                {
                    // transform to body fixed
                    double newPos[3];
                    const char* ref = "J2000";
                    const char* frame = "IAU_ITOKAWA";
                    double i2bmat[3][3];
                    pxform_c(ref, frame, p.time, i2bmat);
                    mxv_c(i2bmat, p.scpos, newPos);

                    fout << 0 << " " << strTime << " " << p.range << " "
                         << newPos[0] << " " << newPos[1] << " " << newPos[2] << " "
                         << p.targetpos[0] << " " << p.targetpos[1] << " " << p.targetpos[2] << " "
                         << p.intersectpos[0] << " " << p.intersectpos[1] << " " << p.intersectpos[2] << "\n";
                }
                else
                {
                    fout << 0 << " " << strTime << " " << p.range << " "
                         << p.scpos[0] << " " << p.scpos[1] << " " << p.scpos[2] << " "
                         << p.targetpos[0] << " " << p.targetpos[1] << " " << p.targetpos[2] << " "
                         << p.intersectpos[0] << " " << p.intersectpos[1] << " " << p.intersectpos[2] << "\n";
                }
            }
            else if (bodyName == "EROS")
            {
                char strTime[32];
                et2utc_c(p.time, "ISOC", 3, 32, strTime);

                double pos[3] = {p.scpos[0], p.scpos[1], p.scpos[2]};
                if(convertFromJ2000)
                {
                    // transform to body fixed
                    const char* ref = "J2000";
                    const char* frame = "IAU_EROS";
                    double i2bmat[3][3];
                    pxform_c(ref, frame, p.time, i2bmat);
                    mxv_c(i2bmat, p.scpos, pos);
                }

                double sclon;
                double sclat;
                double scrdst;
                reclat_c(pos, &scrdst, &sclon, &sclat);
                fout << "0 0 0 0 "
                     << strTime
                     << " 0 0 0 "
                     << (sclon * 180.0 / M_PI) << " "
                     << (sclat * 180.0 / M_PI) << " "
                     << (1000.0 * scrdst)
                     << " 0 0 0 0 0 0\n";
            }
        }

        fout.close();
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }
}
