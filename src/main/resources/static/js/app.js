/**
 * ======================================================================
 * Discussion Forum - Main Application JavaScript // Diskuzní fórum - Hlavní aplikační JavaScript
 * ======================================================================
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 * 
 * Purpose: Handles client-side functionality for the discussion forum application
 * Účel: Zpracovává funkcionalitu na straně klienta pro aplikaci diskuzního fóra
 */

// DOM Content Loaded Event Handler // Obsluha události načtení DOM obsahu
// Initialize application functionality when DOM is fully loaded // Inicializovat funkcionalitu aplikace po úplném načtení DOM
document.addEventListener('DOMContentLoaded', function() {
    
    // Bootstrap Tooltips Initialization // Inicializace Bootstrap tooltipů
    // Initialize Bootstrap tooltips for enhanced user experience // Inicializovat Bootstrap tooltipy pro lepší uživatelskou zkušenost
    if (typeof bootstrap !== 'undefined') {
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    // Auto-dismiss Alert System // Systém automatického zavírání upozornění
    // Automatically hide non-permanent alerts after 5 seconds // Automaticky skrýt nepermanentní upozornění po 5 sekundách
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            if (alert && alert.parentNode) {
                alert.style.transition = 'opacity 0.5s';
                alert.style.opacity = '0';
                setTimeout(() => {
                    if (alert.parentNode) {
                        alert.parentNode.removeChild(alert);
                    }
                }, 500);
            }
        }, 5000);
    });

    // Vote Button Handler // Obsluha tlačítek hlasování
    // Handle vote buttons that are not inside forms (like login links) // Zpracovat tlačítka hlasování, která nejsou uvnitř formulářů (jako odkazy na přihlášení)
    const voteButtons = document.querySelectorAll('.vote-btn:not(form .vote-btn)');
    voteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            // Only handle non-form vote buttons (like login links)
            if (this.tagName === 'A') {
                // This is a login link, let it work normally
                return;
            }
        });
    });

    // Smooth Scrolling Navigation // Plynulá navigace s rolováním
    // Enable smooth scrolling behavior for anchor links // Povolit plynulé rolování pro kotevní odkazy
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            
            // Skip if it's just "#" or empty
            if (targetId === '#' || targetId.length <= 1) {
                return;
            }
            
            try {
                const targetElement = document.querySelector(targetId);
                if (targetElement) {
                    e.preventDefault();
                    targetElement.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            } catch (error) {
                // Skip invalid selectors
                // Skip invalid selectors - debug removed
            }
        });
    });

    // Responsive Navigation Handler // Obsluha responzivní navigace
    // Handle mobile navigation toggle functionality // Zpracovat funkcionalitu přepínání mobilní navigace
    const navToggle = document.querySelector('.navbar-toggler');
    if (navToggle) {
        navToggle.addEventListener('click', function() {
            const navCollapse = document.querySelector('.navbar-collapse');
            if (navCollapse) {
                navCollapse.classList.toggle('show');
            }
        });
    }

    // Dynamic Textarea Resizing // Dynamické změna velikosti textových oblastí
    // Automatically resize textareas based on content // Automaticky změnit velikost textových oblastí podle obsahu
    const textareas = document.querySelectorAll('textarea');
    textareas.forEach(textarea => {
        // Auto-resize on input
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = (this.scrollHeight) + 'px';
        });
        
        // Initial resize
        textarea.style.height = 'auto';
        textarea.style.height = (textarea.scrollHeight) + 'px';
    });

    // Clipboard Copy Functionality // Funkcionalita kopírování do schránky
    // Enable copy-to-clipboard functionality for elements with data-copy attribute // Povolit kopírování do schránky pro elementy s atributem data-copy
    const copyButtons = document.querySelectorAll('[data-copy]');
    copyButtons.forEach(button => {
        button.addEventListener('click', function() {
            const textToCopy = this.getAttribute('data-copy');
            navigator.clipboard.writeText(textToCopy).then(() => {
                // Show success feedback
                const originalText = this.innerHTML;
                this.innerHTML = '<i class="fas fa-check"></i> Copied!';
                this.classList.add('btn-success');
                
                setTimeout(() => {
                    this.innerHTML = originalText;
                    this.classList.remove('btn-success');
                }, 2000);
            });
        });
    });

    // Form Loading State Management // Správa stavu načítání formulářů
    // Show loading indicators during form submission // Zobrazit indikátory načítání během odesílání formuláře
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitButton = this.querySelector('button[type="submit"]');
            if (submitButton && !submitButton.disabled) {
                const originalContent = submitButton.innerHTML;
                submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...';
                submitButton.disabled = true;
                
                // Re-enable if form submission fails (after 10 seconds)
                setTimeout(() => {
                    submitButton.innerHTML = originalContent;
                    submitButton.disabled = false;
                }, 10000);
            }
        });
    });

    // Search Functionality Handler // Obsluha vyhledávací funkcionality
    // Implement debounced search with minimum query length // Implementovat zpožděné vyhledávání s minimální délkou dotazu
    const searchInput = document.querySelector('#searchInput');
    if (searchInput) {
        let searchTimeout;
        
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            const query = this.value.trim();
            
            if (query.length >= 2) {
                searchTimeout = setTimeout(() => {
                    // Implement search functionality here
                    // Implement search functionality here
                }, 300);
            }
        });
    }

    // Infinite Scroll Implementation // Implementace nekonečného rolování
    // Future feature: Load more content as user scrolls // Budoucí funkce: Načíst více obsahu při rolování uživatele
    let isLoading = false;
    
    function handleScroll() {
        if (isLoading) return;
        
        const scrollPosition = window.innerHeight + window.scrollY;
        const documentHeight = document.documentElement.offsetHeight;
        
        if (scrollPosition >= documentHeight - 1000) {
            // Load more content
            isLoading = true;
            // Implement infinite scroll here
            setTimeout(() => {
                isLoading = false;
            }, 1000);
        }
    }
    
    window.addEventListener('scroll', handleScroll);

    // Keyboard Shortcuts Handler // Obsluha klávesových zkratek
    // Implement useful keyboard shortcuts for better user experience // Implementovat užitečné klávesové zkratky pro lepší uživatelskou zkušenost
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + / to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === '/') {
            e.preventDefault();
            const searchInput = document.querySelector('#searchInput, input[type="search"]');
            if (searchInput) {
                searchInput.focus();
            }
        }
        
        // Escape to close modals
        if (e.key === 'Escape') {
            const openModal = document.querySelector('.modal.show');
            if (openModal && typeof bootstrap !== 'undefined') {
                const modalInstance = bootstrap.Modal.getInstance(openModal);
                if (modalInstance) {
                    modalInstance.hide();
                }
            }
        }
    });

    // Theme Switching System // Systém přepínání témat
    // Future feature: Dark/light theme toggle with localStorage persistence // Budoucí funkce: Přepínání tmavého/světlého tématu s uložením do localStorage
    const themeToggle = document.querySelector('#themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', function() {
            document.body.classList.toggle('dark-theme');
            localStorage.setItem('theme', document.body.classList.contains('dark-theme') ? 'dark' : 'light');
        });
        
        // Load saved theme
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-theme');
        }
    }

    // Lazy Image Loading System // Systém líného načítání obrázků
    // Improve performance by loading images only when visible // Zlepšit výkon načítáním obrázků pouze při zobrazení
    const images = document.querySelectorAll('img[data-src]');
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.removeAttribute('data-src');
                    imageObserver.unobserve(img);
                }
            });
        });
        
        images.forEach(img => imageObserver.observe(img));
    } else {
        // Fallback for browsers without IntersectionObserver
        images.forEach(img => {
            img.src = img.dataset.src;
            img.removeAttribute('data-src');
        });
    }

    // Comment Voting Event Delegation // Delegace událostí hlasování komentářů
    // Handle comment voting with event delegation for dynamic content // Zpracovat hlasování komentářů s delegací událostí pro dynamický obsah
    document.addEventListener('click', function(e) {
        if (e.target.closest('.comment-vote-btn')) {
            e.preventDefault();
            const button = e.target.closest('.comment-vote-btn');
            const postId = button.getAttribute('data-post-id');
            const commentId = button.getAttribute('data-comment-id');
            const voteType = button.getAttribute('data-vote-type');
            
            
            if (postId && commentId && voteType) {
                window.voteComment(postId, commentId, voteType, button);
            } else {
                // Missing vote data attributes - handling silently in production
            }
        }
    });

    // Reply Button Event Delegation // Delegace událostí tlačítek odpovědí
    // Handle reply form toggles with event delegation // Zpracovat přepínání formulářů odpovědí s delegací událostí
    document.addEventListener('click', function(e) {
        if (e.target.closest('.reply-btn')) {
            e.preventDefault();
            const button = e.target.closest('.reply-btn');
            const commentId = button.getAttribute('data-comment-id');
            
            
            if (commentId) {
                window.toggleReplyForm(commentId);
            } else {
                // Missing comment ID for reply button - handling silently in production
            }
        }
    });

});

// Global Function Definitions // Definice globálních funkcí
// Make functions globally available for dynamic content interaction // Zpřístupnit funkce globálně pro interakci s dynamickým obsahem

/**
 * Comment Voting Function // Funkce hlasování komentářů
 * Handles AJAX voting for comments with visual feedback // Zpracovává AJAX hlasování komentářů s vizuální zpětnou vazbou
 * @param {string} postId - Post identifier // Identifikátor příspěvku
 * @param {string} commentId - Comment identifier // Identifikátor komentáře
 * @param {string} voteType - Vote type (upvote/downvote) // Typ hlasování (kladné/záporné)
 * @param {Element} button - Button element that triggered the vote // Element tlačítka, které spustilo hlasování
 */
window.voteComment = function(postId, commentId, voteType, button) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
    
    const headers = {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
    };
    
    if (csrfToken && csrfHeaderName) {
        headers[csrfHeaderName] = csrfToken;
    }
    
    // Visual feedback - animate button
    button.style.transform = 'scale(0.9)';
    button.style.transition = 'all 0.15s ease';
    
    const url = `/posts/${postId}/comments/${commentId}/${voteType}`;
    
    fetch(url, {
        method: 'POST',
        headers: headers
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // Update the score display with animation
            const scoreElement = button.parentElement.querySelector('.comment-score');
            if (scoreElement) {
                scoreElement.style.transform = 'scale(1.1)';
                scoreElement.style.transition = 'transform 0.2s ease';
                scoreElement.textContent = data.score;
                
                setTimeout(() => {
                    scoreElement.style.transform = 'scale(1)';
                }, 200);
            }
            
            // Visual feedback - highlight the button briefly
            const originalColor = button.style.color;
            button.style.color = voteType === 'upvote' ? '#10b981' : '#ef4444';
            
            setTimeout(() => {
                button.style.color = originalColor;
            }, 800);
            
        } else {
            // Subtle error indication
            button.style.color = '#ef4444';
            setTimeout(() => {
                button.style.color = '';
            }, 500);
        }
    })
    .catch(error => {
        // Subtle error indication
        button.style.color = '#ef4444';
        setTimeout(() => {
            button.style.color = '';
        }, 500);
    })
    .finally(() => {
        // Reset button scale
        button.style.transform = 'scale(1)';
    });
};

/**
 * Reply Form Toggle Function // Funkce přepínání formuláře odpovědi
 * Shows/hides reply form with smooth animation // Zobrazuje/skrývá formulář odpovědi s plynulou animací
 * @param {string} commentId - Comment identifier to reply to // Identifikátor komentáře, na který odpovědět
 */
window.toggleReplyForm = function(commentId) {
    const replyForm = document.getElementById(`reply-form-${commentId}`);
    
    if (replyForm) {
        const isVisible = replyForm.style.display !== 'none';
        
        if (isVisible) {
            // Hide with animation
            replyForm.style.opacity = '0';
            replyForm.style.transform = 'translateY(-10px)';
            setTimeout(() => {
                replyForm.style.display = 'none';
            }, 200);
        } else {
            // Show with animation
            replyForm.style.display = 'block';
            replyForm.style.opacity = '0';
            replyForm.style.transform = 'translateY(-10px)';
            replyForm.style.transition = 'all 0.2s ease';
            
            setTimeout(() => {
                replyForm.style.opacity = '1';
                replyForm.style.transform = 'translateY(0)';
            }, 50);
            
            // Focus on the textarea when showing the form
            const textarea = replyForm.querySelector('textarea');
            if (textarea) {
                setTimeout(() => {
                    textarea.focus();
                }, 250);
            }
        }
    }
};

// Utility Functions and Namespace // Pomocné funkce a jmenný prostor
// Global namespace for discussion forum utility functions // Globální jmenný prostor pro pomocné funkce diskuzního fóra
window.DiscussionForum = {
    /**
     * Notification Display Function // Funkce zobrazení oznámení
     * Show floating notification with auto-dismiss // Zobrazit plovoucí oznámení s automatickým zavřením
     * @param {string} message - Notification message // Zpráva oznámení
     * @param {string} type - Bootstrap alert type // Typ Bootstrap upozornění
     */
    showNotification: function(message, type = 'info') {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        alertDiv.style.top = '20px';
        alertDiv.style.right = '20px';
        alertDiv.style.zIndex = '9999';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(alertDiv);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 5000);
    },
    
    /**
     * Time Formatting Function // Funkce formátování času
     * Convert date to human-readable "time ago" format // Převést datum do lidsky čitelného formátu "před časem"
     * @param {Date} date - Date to format // Datum k formátování
     * @returns {string} Formatted time string // Formátovaný řetězec času
     */
    timeAgo: function(date) {
        const now = new Date();
        const diffInSeconds = Math.floor((now - date) / 1000);
        
        if (diffInSeconds < 60) return 'just now';
        if (diffInSeconds < 3600) return Math.floor(diffInSeconds / 60) + 'm ago';
        if (diffInSeconds < 86400) return Math.floor(diffInSeconds / 3600) + 'h ago';
        if (diffInSeconds < 2592000) return Math.floor(diffInSeconds / 86400) + 'd ago';
        if (diffInSeconds < 31536000) return Math.floor(diffInSeconds / 2592000) + 'mo ago';
        return Math.floor(diffInSeconds / 31536000) + 'y ago';
    },
    
    /**
     * Text Truncation Function // Funkce zkracování textu
     * Truncate text to specified length with ellipsis // Zkrátit text na zadanou délku s třemi tečkami
     * @param {string} text - Text to truncate // Text ke zkrácení
     * @param {number} length - Maximum length // Maximální délka
     * @returns {string} Truncated text // Zkrácený text
     */
    truncate: function(text, length = 100) {
        if (text.length <= length) return text;
        return text.substring(0, length) + '...';
    }
};

/**
 * Mobile Navigation Functions // Funkce mobilní navigace
 * Handle mobile menu interactions and responsive behavior // Zpracovat interakce mobilního menu a responzivní chování
 */

/**
 * Mobile Menu Toggle Function // Funkce přepínání mobilního menu
 * Toggle mobile navigation menu visibility and state // Přepnout viditelnost a stav mobilního navigačního menu
 */
window.toggleMobileMenu = function() {
    const mobileNav = document.getElementById('mobileNav');
    const toggle = document.querySelector('.mobile-menu-toggle');
    
    if (mobileNav && toggle) {
        mobileNav.classList.toggle('active');
        toggle.classList.toggle('active');
        
        // Prevent body scroll when menu is open
        if (mobileNav.classList.contains('active')) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = '';
        }
    }
};

// Mobile Menu Outside Click Handler // Obsluha kliknutí mimo mobilní menu
// Close mobile menu when user clicks outside of it // Zavřít mobilní menu, když uživatel klikne mimo něj
document.addEventListener('click', function(e) {
    const mobileNav = document.getElementById('mobileNav');
    const toggle = document.querySelector('.mobile-menu-toggle');
    
    if (mobileNav && mobileNav.classList.contains('active')) {
        if (!mobileNav.contains(e.target) && !toggle.contains(e.target)) {
            mobileNav.classList.remove('active');
            toggle.classList.remove('active');
            document.body.style.overflow = '';
        }
    }
});

// Mobile Menu Link Click Handler // Obsluha kliknutí na odkaz v mobilním menu
// Close mobile menu automatically when user clicks on navigation link // Zavřít mobilní menu automaticky při kliknutí na navigační odkaz
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('mobile-nav-link')) {
        const mobileNav = document.getElementById('mobileNav');
        const toggle = document.querySelector('.mobile-menu-toggle');
        
        if (mobileNav && toggle) {
            mobileNav.classList.remove('active');
            toggle.classList.remove('active');
            document.body.style.overflow = '';
        }
    }
});

/**
 * Nested Replies Collapse/Expand Function // Funkce sbalování/rozbalování vnořených odpovědí
 * Toggle visibility of nested reply threads with animation // Přepnout viditelnost vnořených vláken odpovědí s animací
 * @param {string} nestedRepliesId - ID of nested replies container // ID kontejneru vnořených odpovědí
 */
window.toggleNestedReplies = function(nestedRepliesId) {
    const nestedReplies = document.getElementById(nestedRepliesId);
    const toggleBtn = document.querySelector(`[onclick*="${nestedRepliesId}"]`);
    
    if (nestedReplies && toggleBtn) {
        const isVisible = nestedReplies.style.display !== 'none';
        const icon = toggleBtn.querySelector('.collapse-icon');
        const text = toggleBtn.querySelector('.small');
        
        if (isVisible) {
            // Collapse - hide nested replies
            nestedReplies.style.opacity = '0';
            nestedReplies.style.transform = 'translateY(-10px)';
            nestedReplies.style.transition = 'all 0.2s ease';
            
            setTimeout(() => {
                nestedReplies.style.display = 'none';
            }, 200);
            
            // Update button
            icon.className = 'fas fa-chevron-down collapse-icon';
            text.textContent = 'Show replies';
            
        } else {
            // Expand - show nested replies
            nestedReplies.style.display = 'block';
            nestedReplies.style.opacity = '0';
            nestedReplies.style.transform = 'translateY(-10px)';
            nestedReplies.style.transition = 'all 0.2s ease';
            
            setTimeout(() => {
                nestedReplies.style.opacity = '1';
                nestedReplies.style.transform = 'translateY(0)';
            }, 50);
            
            // Update button
            icon.className = 'fas fa-chevron-up collapse-icon';
            text.textContent = 'Hide replies';
        }
    }
};