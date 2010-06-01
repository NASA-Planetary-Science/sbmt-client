# You may need to modify the following paths

SDIR = /home/kahneg1/src/near/eros-data/spice_eros_platelib

SINC = -I$(SDIR)/include
#SLIB =  $(SDIR)/lib/platelib.a $(SDIR)/lib/spicelib.a $(SDIR)/lib/support.a /usr/lib/gcc/i386-redhat-linux/3.4.6/libg2c.a -lm
SLIB =  $(SDIR)/lib/platelib.a $(SDIR)/lib/spicelib.a $(SDIR)/lib/support.a -lgfortran -lm
CFLAGS = -Wall -ansi -pedantic -g -static

NIS_PROG = near_nis

OBJS = convertString.o

all: $(NIS_PROG)

$(NIS_PROG): $(OBJS) $(NIS_PROG).o
	$(CC) -static -o $@ $^ $(SLIB)

.c.o:
	$(CC) $(CFLAGS) $(SINC) -c $<

clean:
	$(RM) $(NIS_PROG) $(OBJS) $(NIS_PROG).o
