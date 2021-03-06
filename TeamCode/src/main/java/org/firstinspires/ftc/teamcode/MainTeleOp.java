        package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.gamepad.ToggleButtonReader;
import com.arcrobotics.ftclib.hardware.RevIMU;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.VoltageSensor;

/**
 * This sample shows how to use dead wheels with external encoders
 * paired with motors that don't require encoders.
 * In this sample, we will use the drive motors' encoder
 * ports as they are not needed due to not using the drive encoders.
 * The external encoders we are using are REV through-bore.
 */
@TeleOp(name = "John Wilkes Booth")
public class MainTeleOp extends LinearOpMode {

    // The lateral distance between the left and right odometers
    // is called the trackwidth. This is very important for
    // determining angle for turning approximations
    public static final double TRACKWIDTH = 8.8;

    // Center wheel offset is the distance between the
    // center of rotation of the robot and the center odometer.
    // This is to correct for the error that might occur when turning.
    // A negative offset means the odometer is closer to the back,
    // while a positive offset means it is closer to the front.
    public static final double CENTER_WHEEL_OFFSET = 8.8;

    public static final double WHEEL_DIAMETER = 1.42;
    // if needed, one can add a gearing term here
    public static final double TICKS_PER_REV = 8192;
    public static final double DISTANCE_PER_PULSE = Math.PI * WHEEL_DIAMETER / TICKS_PER_REV;

    //private UGContourRingPipeline pipeline;
    //private OpenCvCamera camera;

    //private int cameraMonitorViewId;

    private Motor frontLeft, frontRight, backLeft, backRight, shooter, intake;
    private SimpleServo kicker;
    private RevIMU imu;
    GamepadEx gPad;
    private MecanumDrive driveTrain;
    //private Encoder leftOdometer, rightOdometer, centerOdometer;
    //private OdometrySubsystem odometry;
    private ToggleButtonReader buttonReaderY, buttonReaderA, buttonReaderX, buttonReaderB;
    private VoltageSensor voltageSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        frontLeft = new Motor(hardwareMap, "fL");
        frontRight = new Motor(hardwareMap, "fR");
        backLeft = new Motor(hardwareMap, "bL");
        backRight = new Motor(hardwareMap, "bR");

        intake = new Motor(hardwareMap, "intake");
        shooter = new Motor(hardwareMap, "shooter");
        kicker = new SimpleServo(hardwareMap, "kicker", 0.2, 1) {
        };

        driveTrain = new MecanumDrive(frontLeft, frontRight, backLeft, backRight);
        imu = new RevIMU(hardwareMap);

        gPad = new GamepadEx(gamepad1);

        buttonReaderY = new ToggleButtonReader(gPad, GamepadKeys.Button.Y);
        buttonReaderA = new ToggleButtonReader(gPad, GamepadKeys.Button.A);
        buttonReaderX = new ToggleButtonReader(gPad, GamepadKeys.Button.X);
        buttonReaderB = new ToggleButtonReader(gPad, GamepadKeys.Button.B);
        voltageSensor = hardwareMap.voltageSensor.iterator().next();

        // Here we set the distance per pulse of the odometers.
        // This is to keep the units consistent for the odometry.
        //leftOdometer = frontLeft.encoder.setDistancePerPulse(DISTANCE_PER_PULSE);
        //rightOdometer = frontRight.encoder.setDistancePerPulse(-DISTANCE_PER_PULSE);
        //centerOdometer = backLeft.encoder.setDistancePerPulse(DISTANCE_PER_PULSE);

        frontLeft.motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
/*
        odometry = new OdometrySubsystem(new HolonomicOdometry(
                leftOdometer::getDistance,
                rightOdometer::getDistance,
                centerOdometer::getDistance,
                TRACKWIDTH, CENTER_WHEEL_OFFSET
        ));
*/
        imu.init();


        backRight.setInverted(true);
        frontLeft.setInverted(true);
        frontRight.setInverted(true);
        backLeft.setInverted(true);
        shooter.setInverted(true);

        shooter.setRunMode(Motor.RunMode.VelocityControl);
        shooter.setVeloCoefficients(0.6,0.03,0);
/*
        cameraMonitorViewId = this
                .hardwareMap
                .appContext
                .getResources().getIdentifier(
                        "cameraMonitorViewId",
                        "id",
                        hardwareMap.appContext.getPackageName()
                );

        camera = OpenCvCameraFactory
                .getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class, "Jesus"), cameraMonitorViewId);
        camera.setPipeline(pipeline = new UGContourRingPipeline(telemetry, true));
        camera.openCameraDeviceAsync(() -> camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT));
        camera.openCameraDevice();

*/

        waitForStart();

        double x = 1;

        while (opModeIsActive() && !isStopRequested()) {
            if(gamepad1.right_bumper){
                x = 0.25;
            } else {
                x = 1;
            }


            driveTrain.driveRobotCentric(gPad.getLeftX() * x, gPad.getLeftY() * x, gPad.getRightX() * x);

            double voltage = voltageSensor.getVoltage();
            double shooterSpeed = Math.sqrt(12.35/voltage);


            buttonReaderY.readValue();
            buttonReaderA.readValue();
            buttonReaderX.readValue();
            buttonReaderB.readValue();

            if(buttonReaderY.getState()){
                shooter.set(0.81* shooterSpeed);
            }else if(buttonReaderA.getState()){
                shooter.set(0.74 * shooterSpeed);
            }else{
                shooter.set(0);
            }

            if(gamepad1.right_trigger >= 0.75){
                kicker.setPosition(1.0);
                Thread.sleep(200);
                kicker.setPosition(0.67);
            } else if (gamepad1.left_trigger >= 0.75){
                kicker.setPosition(0.2);
            }

            if(buttonReaderX.getState()){
                intake.set(1);
            } else if (buttonReaderB.getState()) {
                intake.set(-1);
            } else {
                intake.set(0);
            }

            //telemetry.addData("Angle: ", kicker.getAngle());
            //telemetry.addData("Stack Height", pipeline.getHeight());
            //telemetry.addData("x", odometry.getPose().getX());
            //telemetry.addData("y", odometry.getPose().getY());
            //telemetry.addData("heading", odometry.getPose().getRotation().getDegrees());
            telemetry.addData("Angle: ", kicker.getAngle());
            telemetry.update();
            //odometry.update();
        }
    }

}