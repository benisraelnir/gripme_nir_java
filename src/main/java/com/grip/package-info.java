/**
 * Grip - Render local readme files before sending off to GitHub.
 *
 * This package provides functionality for rendering GitHub-flavored Markdown files locally.
 * It serves as the main entry point for the Grip application and includes core components
 * for rendering, reading, and managing markdown content.
 *
 * Key components available in this package and its subpackages:
 *
 * Constants (from {@link com.grip.config.GripConstants}):
 * - VERSION (4.6.2)
 * - DEFAULT_API_URL
 * - DEFAULT_FILENAMES
 * - DEFAULT_FILENAME
 * - DEFAULT_GRIPHOME
 * - DEFAULT_GRIPURL
 * - STYLE_ASSET_URLS_INLINE_FORMAT
 * - STYLE_ASSET_URLS_RE
 * - STYLE_ASSET_URLS_SUB_FORMAT
 * - STYLE_URLS_RES
 * - STYLE_URLS_SOURCE
 * - SUPPORTED_EXTENSIONS
 * - SUPPORTED_TITLES
 *
 * Core Classes:
 * - {@link com.grip.core.Grip} - Main application class
 * - {@link com.grip.assets.GitHubAssetManager} - Manages GitHub assets
 * - {@link com.grip.assets.ReadmeAssetManager} - Manages README assets
 *
 * Exception Classes:
 * - {@link com.grip.exceptions.AlreadyRunningException} - Thrown when Grip is already running
 * - {@link com.grip.exceptions.ReadmeNotFoundException} - Thrown when README file is not found
 *
 * Reader Classes:
 * - {@link com.grip.reader.ReadmeReader} - Base class for reading README files
 * - {@link com.grip.reader.DirectoryReader} - Reads from directory
 * - {@link com.grip.reader.StdinReader} - Reads from standard input
 * - {@link com.grip.reader.TextReader} - Reads from text content
 *
 * Renderer Classes:
 * - {@link com.grip.renderer.ReadmeRenderer} - Base class for rendering
 * - {@link com.grip.renderer.GitHubRenderer} - Renders using GitHub API
 * - {@link com.grip.renderer.OfflineRenderer} - Renders offline
 *
 * API Methods (from {@link com.grip.api.GripApi}):
 * - clearCache() - Clears cached styles and assets
 * - createApp() - Creates a Grip application
 * - export() - Exports rendered HTML
 * - renderContent() - Renders specified markup
 * - renderPage() - Renders to HTML page
 * - serve() - Starts server to render files
 *
 * Command Line Interface:
 * - {@link com.grip.command.GripCommandLine} - Handles command line operations
 *
 * @copyright (c) 2014-2022 by Joe Esposito.
 * @license MIT, see LICENSE for more details.
 */
package com.grip;