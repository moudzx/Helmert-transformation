# Helmert-transformation
# Execution Output
<p align="center">
  <img width="100%" alt="output1" src="https://github.com/user-attachments/assets/f7aa3df0-ba51-46ba-99ac-023f1b6b7ba7" />
</p>

<p align="center">
  <img width="100%" alt="output2" src="https://github.com/user-attachments/assets/eace66cc-ee0d-4db1-b4f0-b7a378becfba" />
</p>

# Overview

The Earth's actual shape is complex.. <br>
It's lumpy and irregular due to uneven mass distribution (mountains, ocean trenches, dense rock underground).

In geodesy and geographic information systems (GIS), the Earth is represented by localized reference ellipsoids. Because these reference systems (datums) differ between regions or satellite generations (e.g., mapping old local datums to modern global systems like WGS84), coordinates cannot simply be plotted across them directly.

<p align="center">
  <img width="754" height="384" alt="gcs" src="https://github.com/user-attachments/assets/fb4e9311-b712-479a-9537-3c8001eb5511" />
</p>

GPS uses a global datum (called WGS84), which uses an ellipsoid that is the "best fit" for the entire planet.
Different countries and organizations optimize their own local datums; so when the GPS satalleits produces a raw position in WGS84, this position is transformed into the correct local datums to match the map we use.

This application acts as a specialized geodetic conversion utility. It handles the rigorous, three-step mathematical pipeline necessary to transform positions safely without distortion:

### Transform coordinates
<p align="center">
  <img width="819" height="540" alt="transformation" src="https://github.com/user-attachments/assets/97514ac1-c334-4b10-beff-0444570691e3" />
</p>

1) **Geographic to Geocentric Projection:**
   
   Converts curved surface positions ($\text{Latitude}, \text{Longitude}, \text{Ellipsoidal Height } h$) relative to a chosen source ellipsoid into straight 3D Earth-Centered, Earth-Fixed (ECEF) Cartesian coordinate vectors ($X, Y, Z$ in meters).
   
   <p align="center">
     <img width="554" height="346" alt="1" src="https://github.com/user-attachments/assets/20e04cd6-b6d8-491e-aced-a84aed5bdacf" />
   </p>
  
2) **Helmert 7-Parameter Matrix Transformation:**
   
   Applies a linear 3D transformation matrix using three translation shifts ($T_x, T_y, T_z$), three axis rotation angles ($R_x, R_y, R_z$), and a uniform scale factor correction ($s$, expressed in parts-per-million).
   
   <p align="center">
     <img width="1015" height="532" alt="2" src="https://github.com/user-attachments/assets/ddf104ab-5d62-4f83-b267-28bba4f96ea6" />
   </p>

 
3) **Geocentric to Geographic Backward Projection:**
  
   Inverts the Cartesian positions back onto the destination reference ellipsoid to derive the translated target coordinates ($\text{Latitude}, \text{Longitude}, \text{Height}$).
   
   <p align="center">
     <img width="801" height="407" alt="3" src="https://github.com/user-attachments/assets/5daf17f5-7e37-4c45-82a6-5d31a43cf0bb" />
     <br>
     <img width="272" height="169" alt="4" src="https://github.com/user-attachments/assets/39a4b1f9-01d7-4033-96f0-a8496546db23" />
   </p>

---


## Build

Linux: chmod +x build.sh && ./build.sh

Windows: double click build.bat

## Run

Linux: java -jar helmert.jar

Windows: double click helmert.jar
