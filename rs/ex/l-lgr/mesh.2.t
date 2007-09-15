begin MeshGenerator

	type LGRMeshGenerator
	
	begin CoarseGeometry
		X0 0.0
		Y0 0.0
		Z0 0.0
		
		array Nx
			2
		end
		
		array Dx
			2.0
		end
		
		array Ny
			1
		end
		
		array Dy
			1.0
		end
		
		array Nz
			1
		end
		
		array Dz
			-1.0
		end
	end
	
	begin Refinements
	
		begin Omega1
		
			array CoarseElementIJK
				1 1 1
			end
		
			begin FineGeometry
				array Nx
					20
				end
				
				array Dx
					0.1
				end
				
				array Ny
					10
				end
				
				array Dy
					0.1
				end
				
				array Nz
					10
				end
				
				array Dz
					-0.1
				end
				
				RegionMappingType Uniform
				
			end
			
			begin RockRegionMap
				array Rock
					1
				end
			end
			
			include rockdata.inp
		
		end

		begin Omega2
		
			array CoarseElementIJK
				2 1 1
			end
		
			
			begin FineGeometry
				array Nx
					40
				end
				
				array Dx
					0.05
				end
				
				array Ny
					20
				end
				
				array Dy
					0.05
				end
				
				array Nz
					20
				end
				
				array Dz
					-0.05
				end
				
				RegionMappingType Uniform
				
			end
			
			begin RockRegionMap
				array Rock
					1
				end
			end
			
			include rockdata.inp
			
		end

			
	% Transformation goes here
	
	end
end

% -------------------------------------------------

begin TransmissibilityMethod
	type TPFA

%	quadarea true
end

% -------------------------------------------------

include sources.inp
