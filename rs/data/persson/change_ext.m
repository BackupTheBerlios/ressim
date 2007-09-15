% [new_name] = change_ext (old_name, new_ext)
%
% Convenience routine to keep everything in the path name except the extension
function [new_name] = change_ext (old_name, varargin)
   % if no extension is specified, then nothing is wanted (only stem returned)
   if length (varargin) > 0,       
      new_ext = varargin{1};
      
      % add period if not already specified at beginning
      if new_ext(1) ~= '.',
         new_ext = ['.', new_ext];
      end;
   else
      new_ext = ''; 
   end;
   
   % dissect old name into various parts
   [path, base] = fileparts (old_name);
   
   % compose new name with a different extension
   new_name = fullfile (path, [base, new_ext]);