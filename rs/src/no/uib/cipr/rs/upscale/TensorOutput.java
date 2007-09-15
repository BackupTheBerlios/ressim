package no.uib.cipr.rs.upscale;

import no.uib.cipr.rs.geometry.Tensor3D;

/**
 * A class for formatted output of tensors.
 */
class TensorOutput {

    private Tensor3D k;

    public TensorOutput(Tensor3D k) {
        this.k = k;
    }

    @Override
    public String toString() {
        return String.format("[%.15E,%.15E,%.15E;%.15E,%.15E,%.15E;%.15E,%.15E,%.15E]",
                k.xx(), k.xy(), k.xz(), k.xy(), k.yy(), k.yz(), k.xz(), k.yz(),
                k.zz());
    }

}
