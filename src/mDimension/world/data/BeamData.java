package mDimension.world.data;

/**激光数据类*/
public class BeamData {
    public int length;
    public int wavelengthLevel;
    public float power = 10f;
    public Beam beam;
    public BeamData(Beam l){
        this.length = l.length;
        this.wavelengthLevel = l.energyLevel;
        this.beam = l;
    }

    public BeamData(Beam l, float power){
        this.length = l.length;
        this.wavelengthLevel = l.energyLevel;
        this.power = power;
        this.beam = l;
    }

    public void setPower(float power){
        this.power = power;
    }
}
