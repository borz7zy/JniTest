cmake_minimum_required(VERSION 3.19)
project("jnitest")

find_package(JNI REQUIRED)

add_library(${PROJECT_NAME} SHARED src/main.cpp)
target_include_directories(${PROJECT_NAME} PRIVATE ${JNI_INCLUDE_DIRS})

set(LIBRARY_EXIT_DIR ${CMAKE_BINARY_DIR}/library_exit)

# create folder
file(MAKE_DIRECTORY ${LIBRARY_EXIT_DIR})

set_target_properties(${PROJECT_NAME} PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${LIBRARY_EXIT_DIR}
    LIBRARY_OUTPUT_DIRECTORY ${LIBRARY_EXIT_DIR}
    ARCHIVE_OUTPUT_DIRECTORY ${LIBRARY_EXIT_DIR}
)

set_target_properties(${PROJECT_NAME} PROPERTIES PREFIX "")