
# 
#	File: 	.cshrc
#
# 	Date:	05/29/2014
#
#	Author:	V. A. Mallder
#
#
stty intr '^c' erase '^?' kill '^u'
umask 2

# Set up the prompt

set host=`hostname`
set me=`whoami`
set shorthost=`echo $host | sed 's/.jhuapl.edu//'`
#set prompt="${shorthost}:{%~} "

#This puts username@hostname:directory in the prompt with different colors
set prompt="%{\033[31m%}${me}%{\033[34m%}@%{\033[32m%}${shorthost}:%{\033[00m%}{%~} "

#This puts username@hostname:directory in the prompt without different colors
#set prompt="%{\033[30m%}${me}%{\033[30m%}@%{\033[30m%}${shorthost}:%{\033[00m%}{%~} "

#This puts the current directory in the prompt to 8 subdirectories.
#set prompt="%m[%c8]> "

#setenv TERM=xterm-256color

#echo "TERM is [$TERM]"

#switch ($TERM)
#    case "xterm*":
#
#        # this alias puts the current working directory in the title bar of the xterm
#	alias cwdcmd 'echo -n "\033]0;$cwd\007"'
#
#        breaksw
#    default:
##         echo "not an xterm"
##        set prompt='csh% '
#        breaksw
#endsw

# Default Editor is vim
setenv EDITOR vim




# For SBMT
setenv JAVA_HOME /project/nearsdc/software/java/x86_64/latest
setenv PATH /project/nearsdc/software/usr/bin:$PATH
setenv LD_LIBRARY_PATH /project/nearsdc/software/usr/lib
setenv PATH ${JAVA_HOME}/bin:${PATH}
setenv PATH /project/nearsdc/software/apache-ant/latest/bin:${PATH}
setenv PATH /software/git-2.6.0/bin:${PATH}
setenv PATH /project/nearsdc/software/cmake/latest/bin:${PATH}
setenv PATH /software/pandoc-1.17.0.3/bin:${PATH}
setenv PATH /software/ruby-2.3.0/bin:${PATH}

setenv PATH /project/sbmtpipeline/software/heasoft-6.21/x86_64-unknown-linux-gnu-libc2.12/bin:${PATH}

setenv PYTHONPATH /project/nearsdc/software/spice/pyspice/install
#
#setenv JAVA_HOME /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.71.x86_64/jre
setenv ANT_HOME /usr/share/ant
# This is needed to run ant for sbmt.
setenv CLASSPATH com.sun.tools.javac.Main

setenv SBMTROOT /project/sbmtpipeline/sbmt
setenv SAAVTKROOT /project/sbmtpipeline/saavtk

# Set path for cd commands
set cdpath=(~ /project/sbmtpipeline /project/nearsdc /disks/d0180/htdocs-sbmt)


#setenv PATH ~/bin:${PATH}

# Allow connections from all hosts
xhost + >&! /dev/null

# Set up aliases
alias htdocs 'cd /disks/d0180/htdocs-sbmt'
alias pipeline 'cd /project/sbmtpipeline'
alias nearsdc 'cd /project/nearsdc'
alias ls "ls -F"
alias la "ls -aF"
alias ll "ls -lFg"
alias lla "ls -laFg"
alias h history
alias mroe more
alias mkae make
alias xterm 'xterm -ls -sb -sl 1000 -geometry 80x50&'
alias date 'date -u +"%a %b %e %T %Z %j %Y"'
alias me 'ps -ef | grep sbmt | grep -v grep | grep -v csh | grep -v xterm | grep -v ps | grep -v sshd'

# Set Altimetry Working Group (ALTWG) paths. This points to executables used to generate shape models.
setenv ALTWG_DIR /project/osiris/altwg/altwg-software
setenv PATH ${PATH}:${ALTWG_DIR}/altwg/bin
# Generic Mapping Tools, used by ALTWG software
setenv GMT_HOME ${ALTWG_DIR}/gmt
setenv PATH ${GMT_HOME}/bin:${PATH}

#
# Set up the grid engine
#
unsetenv SGE_ROOT
foreach dir (ge-GE2011-11p1 ge-GE2011.11-11p1)
  if (-e /opt/$dir) then
    setenv LD_LIBRARY_PATH /usr/lib:/opt/$dir/lib/linux-x64/:$LD_LIBRARY_PATH
    source /opt/$dir/default/common/settings.csh
    break
  endif
end
