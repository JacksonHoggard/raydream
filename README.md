# 💡 RayDream: Java Ray Tracer

A simple yet powerful ray tracer implemented in Java. RayDream creates realistic images by simulating the behavior of light rays as they interact with objects in a scene.

![example](example.png)

## Features

- **Ray Tracing Basics:** Implements fundamental ray tracing algorithms, including object intersection, reflection, refraction, and shading.
- **Scene Description:** Define scenes by programmatically constructing objects and lights.
- **Support for Basic Shapes:** Supports rendering of basic geometric shapes such as spheres and boxes.
- **Model Support:** Supports rendering of models in the wavefront obj format.
- **Materials and Textures:** Assign materials and textures to objects to add realism to a scene.
- **Antialiasing:** Reduces aliasing artifacts with built-in sampling techniques.
- **Parallel Rendering:** Utilizes multi-threading for faster rendering of complex scenes.
- **Acceleration Structures:** Makes use of bounding volume hierarchies and adaptive supersampling to reduce computation times.

## Usage
RayDream is designed to be easy to use while still providing flexibility for advanced users. Here's a basic example of a scene in RayDream:

```
+ camera:
width: 1920
height: 1080
fov: 50
aperture: 20
from: -2 1 1
to: 0 0 -2
up: 0 1 0
;

+ ambient:
color: 1 1 1
brightness: 1
;

+ light: sphere
position: 10 10 10
color: 1 1 1
brightness: 10
radius: 2
;

+ object: sphere
transform:
| translation: 0 0 -2
| rotation: 0 0 0
| scale: 1 1 1
/
radius: 0.5
material: glass
| ambient: 0.1
| ior: 1.5
/
;

+ object: sphere
transform:
| translation: 0 0 -2
| rotation: 0 0 0
| scale: 1 1 1
/
radius: 0.45
material: glass
| ambient: 0.1
| ior: 0.6667
/
;

+ object: sphere
transform:
| translation: -0.5 0 -3
| rotation: 0 0 0
| scale: 1 1 1
/
radius: 0.5
material: reflective
| color: 1 0 0
| ambient: 0.1
| lambertian: 0.6
| specular: 0.6
| exponent: 50
| metalness: 0.2
| ior: 0.617
| k: 2.63
/
;
```

## Contributions

Contributions to RayDream are welcome! Whether you want to fix bugs, add new features, or improve documentation, your contributions are greatly appreciated. Just fork the repository, make your changes, and submit a pull request.

## License

This project is licensed under the MIT License.

## Acknowledgments

Resources used for this project.
* [Raytracing CS148 Stanford](https://graphics.stanford.edu/courses/cs148-10-summer/as3/instructions/as3.pdf)
* [Koto's Stack Overflow Comment](https://stackoverflow.com/a/33091767)
* [Phong Illumination Model Cheat Sheet](http://rodolphe-vaillant.fr/entry/85/phong-illumination-model-cheat-sheet)
* [Overview of the Ray-Tracing Rendering Technique](https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-overview/light-transport-ray-tracing-whitted.html)
* [Ray Tracing in One Weekend](https://raytracing.github.io/books/RayTracingInOneWeekend.html)
* [The Nim Ray Tracer Project - Part 4: Calculating Box Normals](https://blog.johnnovak.net/2016/10/22/the-nim-ray-tracer-project-part-4-calculating-box-normals/)
* [Fresnel Term Approximation for Metals](http://cg.iit.bme.hu/~szirmay/fresnel.pdf)
* [Ray Tracer Challenge: Texture Mapping](http://raytracerchallenge.com/bonus/texture-mapping.html)
* [Ray-plane Intersection Princeton Slide](https://www.cs.princeton.edu/courses/archive/fall00/cs426/lectures/raycast/sld017.htm)
* [Scratchapixel: Ray-Tracing: Rendering a Triangle](https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-rendering-a-triangle/barycentric-coordinates.html)
* [Graphics Compendium Raytracing Chapter 34: Transformations](https://graphicscompendium.com/raytracing/12-transformations)
* [Jacco's Blog](https://jacco.ompf2.com/2022/04/13/how-to-build-a-bvh-part-1-basics/)
* [The University of Utah CS 6958 Lecture 8](https://my.eng.utah.edu/~cs6958/slides/Lec8_2.pdf)
* [The University of Utah Advanced Ray Tracing lecture](https://my.eng.utah.edu/~cs4600/lectures/Wk13_AdvancedRayTracing.pdf)