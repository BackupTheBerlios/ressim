% [opts] = default_options (name1, val1, name2, val2, ...)
%
% Generate a set of standard options that will be used for all test runs.
function [opts] = default_options (varargin)
  % options are encoded as a structure where each tunable parameter has an
  % entry with its name. the optional list of parameters are defaults that
  % may be passed conditionally from various branches.
  opts = struct;
  
  % default is of course always to run the case
  opts.('active') = 1;
  
  % Persson's algorithm will perform the refinement globally, by adjusting 
  % positions for a know amount of vertices, whereas the Triangle program
  % will gradually refine the elements as it generates them.
  opts.('persson') = 1;
  
  % allow the Triangle program to insert extra nodes; if we run the
  % Triangle program ourself (by calling make_triangles), then this should
  % be set to get a better mesh. if the program is called by other
  % subroutines such as create_mesh, then they'll set the parameter
  % appropriately themselves.
  opts.('steiner') = 1;
  
  % number of digits to include in point coordinates; all coordinates will be
  % rounded to this precision (to avoid small distances between endpoints which
  % cannot be seen when designing the case interactively).
  opts.('aperture') = 1e-3;
  
  % number of rounds of refinement that should be done on the triangulation
  % this option is only sensible if the Triangle program is allowed to
  % generate Steiner points on its own. Use two level so that refinement
  % will be activated.
  opts.('refine')  = 2;
  
  % minimum angle allowed in triangles; elements with steeper angles 
  % between sides than this are refined further. the angle is given in 
  % degrees. if no number is specified, then the Triangle program has a
  % builtin value for this parameter of 20 degrees. this value is the
  % highest value that is guaranteed to give a result.
  opts.('minangle') = 20;
  
  % maximum area allowed for triangles; elements larger than this size will
  % be refined into smaller ones. the size is given as a relative ratio to
  % the ideal size of a triangle given the desired number of elements
  % within the area specified in the problem (meaning that the option is
  % independent of the problem itself).
  opts.('maxarea')  = 2.0;
  
  % minimum area for refinement to occur; do not split elements that are
  % already smaller than this size any further. using this options requires
  % a patch for the Triangle program.
  opts.('minarea')  = 0.333;
  
  % movement relative to ideal length that is necessary for the algorithm
  % to run Delaunay retriangulation again (i.e. flip some edges and not
  % just move the vertices). the default is to retriangulate every time.
  opts.('retriangulate') = 0;
  
  % relative proximity to the line used by remove_superfluous; the length
  % is relative to the ideal length of a side in the triangle. the value
  % chosen here is one third of the ideal height of the triangle, selected
  % from the foggy reason that the line will intersect with the triangle
  % half-way and then tuned a bit to make the results look good.
  opts.('proximity') = sqrt (3) / 4;
  
  % don't grant extra rights to points that are on the border; attempt to
  % remove all interior points that are too close to the fracture edges.
  opts.('border') = 0;
  
  % smoothing method that should be used; 'truss' is the one that is implemented
  % in Persson's article which only uses repelling forces. 'bossen' is a
  % particle based method which generates somewhat better results.
  opts.('smoothing') = 'truss';
  
  % how much larger than the ideal length the edge must be without exerting 
  % any force anymore. this parameter is called Fscale in Persson's article
  opts.('scale') = 1.2;
  
  % fraction of the resulting force that is applied in each timestep before
  % the positions are reevaluated. this parameter is called deltat in 
  % Persson's article.
  opts.('timestep') = 0.2;
  
  % movement threshold relative to the ideal length that is necessary for
  % the algorithm to be considered converged. this parameter is called 
  % dptol in Persson's article
  opts.('tolerance') = 0.05;
  
  % maximum number of iterations before we bail and call it a day anyway
  opts.('maxiter') = 20;
  
  % the value chosen here gives the case that was supplied as an example
  % for the Frac3D input file.
  opts.('seed') = 0.230975101;
  
  % square footage of fractures in a cube foot of volume. since the
  % fractures are dimensionally reduced and thus have no aperature, this 
  % setting may be arbitrary large.
  opts.('density') = 0.2;
  
  % number of elements we would like to end up with in the final mesh. the
  % actual number will differ from this one, but should be close. 
  opts.('elements') = 3000;
  
  % if this option is enabled, then the final mesh from the triangulation
  % will not be deleted, but will remain on disk for use later.
  opts.('keep') = 0;
  
  % only non-zero-length identifiers are allowed; we use a blank one as 
  % default to prohibit it being appended to the filenames
  opts.('id') = '';
  
  % if this flag is set, then each figure will be saved to a filename
  % matching the pattern in the companion option. the first pattern is the
  % identity of the setup, the second one is the running number.
  opts.('save') = 0;  
  opts.('filename') = 'fig%s-%02d.eps';
  
  % options that are passed to the plot command when drawing the fracture
  % constraint edges. this is actually the entire palette.
  opts.('color') = 'rgcmy'; % red, green, cyan, magenta, yellow
  opts.('line') = 0.5;
    
  % get the directory to this function; this is the default
  mydir = fileparts (which (mfilename ()));
  opts.('libdir') = fullfile (mydir, '..', '..', 'lib');
  
  % don't perform any clipping on the domain by default
  opts.('clip') = [];
  opts.('size') = [];
  
  % override defaults specified here with those passed on the command line
  opts = merge_structs (struct (varargin{:}), opts);
