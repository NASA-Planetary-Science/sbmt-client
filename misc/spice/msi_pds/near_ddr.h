#ifndef NEAR_DDR_H_
#define NEAR_DDR_H_

/* MOLA data directory */
#define MOLA_DIR "/project/crism/software/crism_ddr/MOLA" 

/* PDS fill value */
#define PDS_NA  -1.e32f
#define PDS_UNK  1.e32f

/* DDR data dimensions */
#define NUM_LAYER   14
#define TOTAL_PIXEL 640

/* MRO SPACECRAFT NAIF ID */
#define MRO_SPACECRAFT -74000

/* MARS ID */
#define MARS     499
#define TIMELEN   51
#define AMPMLEN   51

/* CRISM Low Resolution Clock NAIF ID */
#define MRO_SCLK -74

/* CRISM Fine Resolution Clock NAIF ID */
#define CRISM_SCLK  -74999

/* CRISM C-Kernel NAIF ID */
#define MRO_CRISM_ART    -74012

/* CRISM Frame */
#define CRISM_FRAME "MRO_CRISM_BASE"

/* C-Kernel Comment Size */
#define CMMSIZE  1024

/* Instrument type */
enum instType {VNIR, IR};

/* SCLK tick is equal to 1/65336 of a seconds */
#define SCLKTICKS 65336

/* Maximum number of bursts */
#define MAXTAB   4096

/* Maximum number of input directories */
#define MAXDIR    128

/* Maximum number of fullpath file name length */
#define MAXSTR    256

/* Maximum number of record in a burst */
#define MAXREC   16000

#define MAXBUF   (MAXREC * 2)

/* Maximum DDR Record 640 * 200 * 14 = 1792000 */
#define MAXDDR  2000000

/* Maximum number of records in one segment */
#define MAXSEGREC 65536

#define MAXLIMIT  (MAXSEGREC * 10 / 9)
typedef struct Tab {
    char *filename;
    double sclkdp;
} TAB;

/* Tab Record */
typedef struct Record {
    double et;
    double sclkdp;
    int exposure;
    int rate;
    int scanMode;
} RECORD;

/* Maximum counts for PI */
#define MAXCOUNT 4194303

#define NUM_LINES 412
/*#define NUM_LINES 244*/

#define NUM_LINE_SAMPLES 537

#endif				/* NEAR_DDR_H_ */
