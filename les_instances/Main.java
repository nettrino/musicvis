
import ddf.minim.*; // minim library
import processing.pdf.*; // pdf export library
import java.util.Calendar; // java timestamp
import ddf.minim.analysis.*;
import controlP5.*;
import ddf.minim.*;
import java.util.Random;

ControlP5 cp5;
Random rand;
Minim minim;
AudioInput sound;
AudioPlayer song;
FFT         fft;

float amplitude;
float sensitivity;

float hvalue;

float zp = 0;

IntList xpos;
IntList ypos;
IntList zpos;
IntList strw;
IntList trans;
//newpulse does the light travelling
float newpulsex = 0;
float newpulsey = 0;
float newpulsez = 0;

float distcount = 2000;

static float variance(float a[],
  int n)
{
  // Compute mean (average
  // of elements)
  float sum = 0;

  for (int i = 0; i < n; i++)
    sum += a[i];
  float mean = (float)sum /
    (float)n;

  // Compute sum squared
  // differences with mean.
  float sqDiff = 0;
  for (int i = 0; i < n; i++)
    sqDiff += (a[i] - mean) *
      (a[i] - mean);

  return (float)sqDiff / n;
}

void setup() {
  size(800, 800, P3D);
  cp5 = new ControlP5(this);
  minim = new Minim(this);
  song = minim.loadFile("lesinstances.mp3"); // load song
  fft = new FFT( song.bufferSize(), song.sampleRate() );
  song.play(); // play song
  rand = new Random();
  xpos = new IntList();
  ypos = new IntList();
  zpos = new IntList();
  strw = new IntList();
  trans = new IntList();

  // amplitude for light
  cp5.addSlider("amplitude")
    .setPosition(50, 50)
    .setRange(0, 200)
    .setSize(200, 20)
    .setValue(100)
    .setColorActive(color(80))
    .setColorBackground(color(20))
    .setColorForeground(color(60))
    ;

  // sensitivity for when light will cick in
  cp5.addSlider("sensitivity")
    .setPosition(450, 50)
    .setRange(0, 500)
    .setSize(200, 20)
    .setValue(200)
    .setColorActive(color(100))
    .setColorBackground(color(20))
    .setColorForeground(color(60))
    ;

  xpos.append(0);
  ypos.append(0);
  zpos.append(0);
  strw.append(1);
  strw.append(50);
}

void draw() {
  background(0);
  fill(255);
  pushMatrix();
  fft.forward(song.mix);

  ///simulated camera//////////////////
  translate(width/2, height/2, zp);
  rotateY(PI*2*(mouseX-220)/(width-220));
  rotateX(PI*2*mouseY/height);
  ///end camera//////////////////


  /////brownian motion///////

  int newx = round(random((hvalue/5*-1), (hvalue/5)));
  int newy = round(random((hvalue/5*-1), (hvalue/5)));
  int newz = round(random((hvalue/5*-1), (hvalue/5)));

  int lastx = xpos.get(xpos.size()-1);
  int lasty = ypos.get(ypos.size()-1);
  int lastz = zpos.get(zpos.size()-1);

  if (((lastx + newx) > 400) || ((lastx + newx) < -400)) {
    newx = newx * -1;
  }

  if (((lasty + newy) > 400) || ((lasty + newy) < -400)) {
    newy = newy * -1;
  }

  if (((lastz + newz) > 400) || ((lastz + newz) < -400)) {
    newz = newz * -1;
  }

  // draw fft
  float maxFFT = 0;
  int fftSpecSize = fft.specSize();
  float fftBands[] = new float[fftSpecSize];
  for (int i = 0; i < fftSpecSize; i++)
  {
    float fftHeight = fft.getBand(i) * 8;
    fftBands[i] = fftHeight;
      if (fftHeight > maxFFT) {
      maxFFT = fftHeight;
    }
  }


  int randomNumber = rand.nextInt(3 - 1) + 1;
  if (randomNumber > 2) {
    newx = lastx + fftSpecSize;
    newy = lasty + int(maxFFT);
    newz = lastz + int(variance(fftBands, fftSpecSize))*10;
  } else {
    newx = lastx + newx;
    newy = lasty + newy;
    newz = lastz + newz;
  }
  System.out.printf("%f %f\n", maxFFT, sensitivity);
  //if(hvalue > sensitivity){

  if (hvalue > sensitivity) {
    newpulsex = random(-400, 400);
    newpulsey = random(-400, 400);
    newpulsez = random(-400, 400);
    distcount = 0;
  }
  // this shows the matrix
  xpos.append(newx);
  ypos.append(newy);
  zpos.append(newz);
  strw.append(1);
  trans.append(50);


  // light travelling
  for (int i = 0; i < xpos.size() - 1; i++) {
    float dist = dist(newpulsex, newpulsey, newpulsez, xpos.get(i), ypos.get(i), zpos.get(i));

    if (abs(dist-distcount) < 10 || maxFFT > sensitivity) {
      if (abs(dist-distcount) < 10) {
        strw.set(i, 5);
      }

      trans.set(i, 255);
    }
    strw.set(i, int(strw.get(i)*0.99999));
    trans.set(i, int(trans.get(i)*0.95));

    if (strw.get(i) < 1) {
      strw.set(i, 1);
    }

    if (trans.get(i) < 50) {
      trans.set(i, 50);
    }
    stroke(255, 255, 255, trans.get(i));
    strokeWeight(strw.get(i));
    line(xpos.get(i), ypos.get(i), zpos.get(i), xpos.get(i+1), ypos.get(i+1), zpos.get(i+1));
  }
  popMatrix();

  ////end brownian motion///////


  /////get sound wave///////
  stroke(255, 255, 255, 100);
  strokeWeight(1);

  hvalue = 0;
  for (int i = 0; i < song.mix.size() - 1; i++) {
    line(i, 700 + song.mix.get(i)*amplitude, i+1, 700 + song.mix.get(i+1)*amplitude);
    if (hvalue <  abs(song.mix.get(i))) {
      hvalue = abs(song.mix.get(i));
    }
  }

  hvalue = hvalue * amplitude;
  ////sound bar////
  //noStroke();
  //rectMode(CORNER);
  //fill(20);
  //rect(50, 110, 50, 200);
  //fill(60);
  //rect(50, 310, 50, (hvalue*-1));

  stroke(255);
  //line(110, (310-sensitivity), 120, (310-sensitivity));
  for (int i = 0; i < fftSpecSize; i++)
  {
    // draw the line for frequency band i, scaling it up a bit so we can see it
    line( i, height, i, height - fftBands[i]);
  }
  distcount =  distcount+10;
}

void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  zp = zp - e*5;
}
