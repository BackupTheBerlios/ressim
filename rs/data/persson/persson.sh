#!/bin/sh
# from which directory is this program run? pass this path to Matlab 
# so that it knows where to load the scrips
PROGDIR=$(dirname $(readlink -f $0))

# use arguments from our own command line
ARGS=$@

# figure out if we are running in batch mode or not
if $(echo $ARGS | grep "\-\-input" >/dev/null); then
	PREFIX=""; SUFFIX=""
else
	PREFIX="display_statistics("; SUFFIX=")"
	# unless it is specified on the command-line, turn off visualization
	if $(echo $ARGS | grep -v "\-\-visual" >/dev/null); then
		ARGS="--visual=0 ${ARGS}"
	fi
fi

# first pipe is for converting GNU-style parameters (--x=y) 
# to Matlab-style ('x','y'); second pipe is for adding commas
# between parameters instead of space
ARGS=$(echo $ARGS | sed "s/--\([^=]*\)=\([^-\ ]*\)/'\\1','\\2'/g" | sed "s/'\ *'/','/g")

# remove quotes from number arguments
ARGS=$(echo $ARGS | sed "s/'\([0-9]\+\(\.[0-9]*\([eE][\+-]\?[0-9]\+\)\?\)\?\)'/\1/g")

# launch matlab passing all the commands sent on the command-line
# drop the copyright notice from the output
(matlab -nosplash -nodesktop -nojvm -r \
    "addpath(genpath('$PROGDIR'));${PREFIX}main(${ARGS})${SUFFIX}; quit" | \
    tail -n +11) </dev/null
