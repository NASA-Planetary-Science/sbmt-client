# You may need to modify the following paths

SDIR = /home/kahneg1/src/near/eros-data/spice_eros_platelib

SINC = -I$(SDIR)/include
#SLIB =  $(SDIR)/lib/platelib.a $(SDIR)/lib/spicelib.a $(SDIR)/lib/support.a /usr/lib/gcc/i386-redhat-linux/3.4.6/libg2c.a -lm
SLIB =  $(SDIR)/lib/platelib.a $(SDIR)/lib/spicelib.a $(SDIR)/lib/support.a -lgfortran -lm
CFLAGS = -Wall -ansi -pedantic -g -static

MSI_PROG = near_ddr
NIS_PROG = near_nis

OBJS = writeDdr.o writeLabel.o getEt.o convertString.o getScOrientation.o getSunPosition.o

all: $(MSI_PROG) $(NIS_PROG)

$(MSI_PROG): $(OBJS) $(MSI_PROG).o
	$(CC) -static -o $@ $^ $(SLIB)

$(NIS_PROG): $(OBJS) $(NIS_PROG).o
	$(CC) -static -o $@ $^ $(SLIB)

#writeDdr.o writeLabel.o: near_ddr.h

.c.o:
	$(CC) $(CFLAGS) $(SINC) -c $<

clean:
	$(RM) $(MSI_PROG) $(NIS_PROG) $(OBJS) $(MSI_PROG).o $(NIS_PROG).o
